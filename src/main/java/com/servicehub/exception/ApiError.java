package com.servicehub.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Formato padronizado para erros da API")
public record ApiError(
        @Schema(example = "2026-09-22T23:00:00") LocalDateTime timestamp,
        @Schema(example = "404") int status,
        @Schema(example = "Not Found") String error,
        @Schema(example = "Cliente com ID 99 não encontrado") String message,
        Map<String, String> details
) {}
