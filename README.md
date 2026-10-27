# ServiceHub API — Checkpoint

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

## Observação para o checkpoint

Este checkpoint implementa o primeiro recurso funcional do domínio ServiceHub. As demais entidades e funcionalidades podem ser incorporadas progressivamente nas próximas etapas do projeto.
