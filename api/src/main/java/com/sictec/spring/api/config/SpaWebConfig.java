package com.sictec.spring.api.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration 
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve los estáticos y redirige rutas desconocidas a index.html para React
        // Router
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        // Si existe el archivo estático (CSS, JS, imagen), lo entrega
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // No interceptar rutas de API, Swagger o consola H2
                        if (resourcePath.startsWith("api") 
                                || resourcePath.startsWith("swagger-ui") 
                                || resourcePath.startsWith("v3/api-docs") 
                                || resourcePath.startsWith("h2-console")) {
                            return null;
                        }

                        // Si es una ruta de frontend (ej. /lotes/uuid), entrega index.html
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }

}
