package com.sictec.spring.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SubirLoteResponseDto(@JsonProperty("batch_id") String batchId,
        @JsonProperty("total_filas") int totalFilas,
        @JsonProperty("rucs_unicos") int rucsUnicos,
        @JsonProperty("duplicados") int duplicados) {

}
