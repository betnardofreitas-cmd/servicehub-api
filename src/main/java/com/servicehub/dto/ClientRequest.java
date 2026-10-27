package com.servicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessários para criar ou atualizar um cliente")
public class ClientRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    @Schema(description = "Nome completo", example = "Bernardo Faria", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres")
    @Schema(description = "E-mail válido", example = "bernardo@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "O telefone é obrigatório")
    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    @Pattern(regexp = "^[0-9()+\\- ]{8,20}$", message = "Telefone deve conter apenas números, espaços, parênteses, + ou -")
    @Schema(description = "Telefone para contato", example = "(62) 99999-9999", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    public ClientRequest() {}

    public ClientRequest(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
