package com.sictec.spring.api.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Utiliza los Virtual Threads nativos de Java 21 (Project Loom).
     * Permite lanzar cientos de consultas HTTP concurrentes con consumo mínimo de
     * RAM.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
