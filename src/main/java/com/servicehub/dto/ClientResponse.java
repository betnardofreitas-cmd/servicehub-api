package com.servicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados retornados pela API para um cliente")
public record ClientResponse(
        @Schema(description = "ID do cliente", example = "1") Long id,
        @Schema(description = "Nome do cliente", example = "Bernardo Faria") String name,
        @Schema(description = "E-mail do cliente", example = "bernardo@email.com") String email,
        @Schema(description = "Telefone do cliente", example = "(62) 99999-9999") String phone
) {}
