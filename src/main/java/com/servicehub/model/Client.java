package com.servicehub.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "clients")
@Schema(description = "Cliente cadastrado no ServiceHub")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do cliente", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 120)
    @Schema(description = "Nome completo do cliente", example = "Bernardo Faria")
    private String name;

    @Column(nullable = false, unique = true, length = 160)
    @Schema(description = "E-mail do cliente", example = "bernardo@email.com")
    private String email;

    @Column(nullable = false, length = 20)
    @Schema(description = "Telefone do cliente", example = "(62) 99999-9999")
    private String phone;

    public Client() {}

    public Client(Long id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
