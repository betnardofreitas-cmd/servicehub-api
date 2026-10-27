# ServiceHub API

API REST desenvolvida em Java 21 + Spring Boot para o projeto PBL ServiceHub.

## Objetivo do checkpoint

Implementar uma primeira entrega funcional do ServiceHub com:

- Spring Boot;
- Spring Web;
- Spring Data JPA;
- PostgreSQL;
- SpringDoc OpenAPI / Swagger;
- CRUD completo de Clientes;
- validação de dados;
- tratamento de erros 400, 404 e 409;
- separação Controller / Service / Repository.

## Tecnologias

- Java 21
- Spring Boot 3.5.6
- Maven
- PostgreSQL 16
- Spring Data JPA / Hibernate
- SpringDoc OpenAPI

## Pré-requisitos

- JDK 21
- Maven 3.9+
- Docker e Docker Compose

## Como executar

1. Suba o PostgreSQL:

```bash
docker compose up -d
```

2. Execute a API:

```bash
mvn spring-boot:run
```

Ou, no IntelliJ IDEA, execute `ServicehubApiApplication`.

A API ficará disponível em:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## CRUD de Clientes

### POST /api/clients

```json
{
  "name": "Bernardo Faria",
  "email": "bernardo@email.com",
  "phone": "(62) 99999-9999"
}
```

Retorno esperado: `201 Created`.

### GET /api/clients

Retorno esperado: `200 OK`.

### GET /api/clients/{id}

Retorno esperado: `200 OK`.

Cliente inexistente: `404 Not Found`.

### PUT /api/clients/{id}

```json
{
  "name": "Bernardo Faria Atualizado",
  "email": "bernardo.novo@email.com",
  "phone": "(62) 98888-8888"
}
```

Retorno esperado: `200 OK`.

### DELETE /api/clients/{id}

Retorno esperado: `204 No Content`.

## Tratamento de erros

- `400 Bad Request`: campos obrigatórios ou formato inválido.
- `404 Not Found`: cliente não encontrado.
- `409 Conflict`: e-mail já utilizado.

## Estrutura

```text
src/main/java/com/servicehub/
├── controller/
├── service/
├── repository/
├── model/
├── dto/
├── exception/
└── config/
```

## Fase 2 — Segurança, JWT, GraphQL e testes (Aulas 8, 9 e 10)

Ver seção **Fase 2** abaixo.

## Observação para o checkpoint

Este checkpoint implementa o primeiro recurso funcional do domínio ServiceHub. As demais entidades e funcionalidades podem ser incorporadas progressivamente nas próximas etapas do projeto.


---

## Fase 2

### Aula 8 — Segurança e validação
- `config/SecurityConfig`: `SecurityFilterChain` stateless, CSRF desabilitado (API com token), rotas públicas `/api/auth/**`, Swagger e `/graphiql`; demais rotas exigem autenticação.
- Senhas com `BCryptPasswordEncoder` (entidade `AppUser`, coluna `password_hash`).
- Bean Validation nos DTOs (`@NotBlank`, `@Email`, `@Size`, `@Pattern`, `@NotNull`) + `GlobalExceptionHandler` (400, 401, 403, 404, 409, 500 no formato `ApiError`).
- Sanitização em `util/InputSanitizer` (Apache Commons Text): trim, escape de HTML, e-mail em minúsculas. Aplicada no `ClientService` e `AuthService`.
- `@PreAuthorize` por método nos controllers (`@EnableMethodSecurity`).

### Aula 9 — JWT, papéis e endpoint alternativo
- `POST /api/auth/login` → JWT HS256 (jjwt 0.12) com claim `roles`. `POST /api/auth/register` cria PROVIDER ou CLIENT (ADMIN é recusado com 400).
- `security/JwtAuthFilter` (OncePerRequestFilter) valida `Authorization: Bearer <token>` e popula o `SecurityContextHolder`.
- ADMIN inicial criado pelo `DataInitializer` (variáveis `ADMIN_EMAIL` / `ADMIN_PASSWORD`; padrão `admin@servicehub.com` / `Admin@123`).

| Rota | ADMIN | PROVIDER | CLIENT | sem token |
|---|---|---|---|---|
| `POST /api/clients` | 201 | 403 | 403 | 401 |
| `GET /api/clients` | 200 | 200 | 403 | 401 |
| `GET /api/clients/{id}` | 200 | 200 | 200 | 401 |
| `PUT /api/clients/{id}` | 200 | 403 | 403 | 401 |
| `DELETE /api/clients/{id}` | 204 | 403 | 403 | 401 |
| `GET /api/admin/users` | 200 | 403 | 403 | 401 |

- **GraphQL** em `POST /graphql` (schema `src/main/resources/graphql/schema.graphqls`, controller `graphql/ClientGraphQLController`), com as mesmas regras de papel. Playground: `http://localhost:8080/graphiql`.

```graphql
query { clients { id name email } }
mutation { createClient(input: {name: "Ana", email: "ana@email.com", phone: "(62) 99999-9999"}) { id } }
```

### Aula 10 — Testes
```bash
mvn test
```
Usa H2 em memória (`src/test/resources/application.properties`), não precisa de Docker.

| Classe | Tipo | O que verifica |
|---|---|---|
| `service/ClientServiceTest` | Unitário (Mockito) | sanitização, 409 de e-mail duplicado, 404, delete |
| `service/AuthServiceTest` | Unitário (Mockito) | recusa de ADMIN, senha salva como hash, login gera token |
| `security/JwtServiceTest` | Unitário | token válido, adulterado e expirado |
| `controller/ClientControllerTest` | MockMvc (`@WebMvcTest`) | 201, 400, 401, 403, 404 por papel |
| `integration/AuthFlowIntegrationTest` | Integração (`@SpringBootTest`) | login → token → rotas por papel, BCrypt, GraphQL |
