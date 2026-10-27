package com.servicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de login")
public record LoginRequest(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        @Schema(example = "admin@servicehub.com") String email,

        @NotBlank(message = "A senha é obrigatória")
        @Schema(example = "Admin@123") String password
) {}
