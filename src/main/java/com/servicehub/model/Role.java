package com.servicehub.model;

/**
 * Papéis do ServiceHub.
 * ADMIN    - acesso total (gestão de clientes e usuários)
 * PROVIDER - prestador de serviço: consulta clientes
 * CLIENT   - cliente: consulta apenas por ID
 */
public enum Role {
    ADMIN,
    PROVIDER,
    CLIENT
}
