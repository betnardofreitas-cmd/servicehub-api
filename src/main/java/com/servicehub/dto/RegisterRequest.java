package com.servicehub.dto;

import com.servicehub.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Cadastro de usuário. Apenas PROVIDER e CLIENT podem se auto-cadastrar.")
public record RegisterRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        @Schema(example = "Maria Prestadora") String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres")
        @Schema(example = "maria@servicehub.com") String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha deve conter letras e números")
        @Schema(example = "Senha123") String password,

        @NotNull(message = "O papel é obrigatório (PROVIDER ou CLIENT)")
        @Schema(example = "PROVIDER") Role role
) {}
