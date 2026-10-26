package com.sictec.spring.api.service;

import com.sictec.spring.api.dto.SriContribuyenteDto;
import com.sictec.spring.api.exception.SriComunicacionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests UNITARIOS de SriConsultaService.
 *
 * Se usa Mockito para simular el RestClient completo sin levantar Spring
 * ni hacer llamadas HTTP reales. Esto fue posible gracias al refactor que
 * inyecta el RestClient por constructor en lugar de construirlo internamente.
 *
 * Patrón de mock para RestClient (API fluida):
 *   restClient.get() → RequestHeadersUriSpec
 *     .uri(...)       → RequestHeadersSpec
 *     .retrieve()     → ResponseSpec
 *     .body(...)      → T
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SriConsultaService — Tests Unitarios")
class SriConsultaServiceTest {

    // ── Mocks de la cadena fluida de RestClient ───────────────────────────
    private RestClient restClient;
    private RestClient.RequestHeadersUriSpec<?> uriSpec;
    private RestClient.RequestHeadersSpec<?> headersSpec;
    private RestClient.ResponseSpec responseSpec;

    private SriConsultaService sriService;

    @BeforeEach
    void setUp() {
        restClient   = mock(RestClient.class);
        uriSpec      = mock(RestClient.RequestHeadersUriSpec.class);
        headersSpec  = mock(RestClient.RequestHeadersSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        // Encadenar los mocks para que get().uri(...).retrieve() funcione
        doReturn(uriSpec).when(restClient).get();
        doReturn(headersSpec).when(uriSpec).uri(any(java.util.function.Function.class));
        doReturn(responseSpec).when(headersSpec).retrieve();

        sriService = new SriConsultaService(restClient);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // validar(ruc)
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("validar(ruc)")
    class ValidarTests {

        @Test
        @DisplayName("devuelve true cuando el SRI confirma que el RUC existe")
        void devuelveTrue_cuandoSriRespondeTrue() {
            when(responseSpec.body(Boolean.class)).thenReturn(Boolean.TRUE);

            boolean resultado = sriService.validar("1791234567001");

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("devuelve false cuando el SRI dice que el RUC NO existe")
        void devuelveFalse_cuandoSriRespondeFalse() {
            when(responseSpec.body(Boolean.class)).thenReturn(Boolean.FALSE);

            boolean resultado = sriService.validar("0000000000000");

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("devuelve false cuando el SRI devuelve null (respuesta inesperada)")
        void devuelveFalse_cuandoSriRespondeNull() {
            when(responseSpec.body(Boolean.class)).thenReturn(null);

            boolean resultado = sriService.validar("1791234567001");

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("lanza SriComunicacionException cuando el SRI no responde (timeout/red)")
        void lanzaSriComunicacionException_cuandoFallaLaRed() {
            when(responseSpec.body(Boolean.class))
                    .thenThrow(new RuntimeException("Connection timed out"));

            assertThatThrownBy(() -> sriService.validar("1791234567001"))
                    .isInstanceOf(SriComunicacionException.class)
                    .hasMessageContaining("1791234567001")
                    .hasMessageContaining("Connection timed out");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // consultar(ruc)
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("consultar(ruc)")
    class ConsultarTests {

        private static final String RUC_VALIDO = "1791234567001";

        @Test
        @DisplayName("devuelve el primer contribuyente cuando el SRI responde con resultados")
        void devuelveContribuyente_cuandoSriEnuentraElRuc() {
            SriContribuyenteDto contribuyente = new SriContribuyenteDto(
                    RUC_VALIDO,
                    "EMPRESA DEMO S.A.",
                    "Actividades de programación informática"
            );
            when(responseSpec.body(any(ParameterizedTypeReference.class)))
                    .thenReturn(List.of(contribuyente));

            SriContribuyenteDto resultado = sriService.consultar(RUC_VALIDO);

            assertThat(resultado).isNotNull();
            assertThat(resultado.numeroRuc()).isEqualTo(RUC_VALIDO);
            assertThat(resultado.actividadEconomicaPrincipal())
                    .isEqualTo("Actividades de programación informática");
        }

        @Test
        @DisplayName("devuelve null cuando el SRI responde con lista vacía")
        void devuelveNull_cuandoSriNoEncuentraElRuc() {
            when(responseSpec.body(any(ParameterizedTypeReference.class)))
                    .thenReturn(List.of());

            SriContribuyenteDto resultado = sriService.consultar("9999999999999");

            assertThat(resultado).isNull();
        }

        @Test
        @DisplayName("devuelve null cuando el SRI devuelve body null")
        void devuelveNull_cuandoSriRespondeNull() {
            when(responseSpec.body(any(ParameterizedTypeReference.class)))
                    .thenReturn(null);

            SriContribuyenteDto resultado = sriService.consultar(RUC_VALIDO);

            assertThat(resultado).isNull();
        }

        @Test
        @DisplayName("lanza SriComunicacionException cuando falla la conexión al SRI")
        void lanzaSriComunicacionException_cuandoFallaLaConexion() {
            when(responseSpec.body(any(ParameterizedTypeReference.class)))
                    .thenThrow(new RuntimeException("Read timed out"));

            assertThatThrownBy(() -> sriService.consultar(RUC_VALIDO))
                    .isInstanceOf(SriComunicacionException.class)
                    .hasMessageContaining(RUC_VALIDO);
        }

        @Test
        @DisplayName("devuelve solo el primer resultado cuando el SRI devuelve múltiples")
        void devuelvePrimerResultado_cuandoSriDevuelveVariosContribuyentes() {
            SriContribuyenteDto primero  = new SriContribuyenteDto(RUC_VALIDO, "EMPRESA A", "Comercio");
            SriContribuyenteDto segundo = new SriContribuyenteDto(RUC_VALIDO, "EMPRESA B", "Servicios");

            when(responseSpec.body(any(ParameterizedTypeReference.class)))
                    .thenReturn(List.of(primero, segundo));

            SriContribuyenteDto resultado = sriService.consultar(RUC_VALIDO);

            assertThat(resultado.razonSocial()).isEqualTo("EMPRESA A");
        }
    }
}
