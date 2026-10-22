package com.sictec.spring.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EstadoLoteResponseDto(@JsonProperty("total_jobs") int totalJobs,
        @JsonProperty("pending_jobs") int pendingJobs,
        @JsonProperty("processed_jobs") int processedJobs,
        @JsonProperty("failed_jobs") int failedJobs,
        @JsonProperty("finished") boolean finished,
        @JsonProperty("cancelled") boolean cancelled) {

}
