package com.servicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT emitido no login")
public record AuthResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...") String token,
        @Schema(example = "Bearer") String type,
        @Schema(example = "3600") long expiresInSeconds,
        @Schema(example = "ADMIN") String role
) {}
