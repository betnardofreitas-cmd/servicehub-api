# Relatório de andamento — ServiceHub API (fase 2)

## 1. Identificação

- **Repositório:** https://github.com/betnardofreitas-cmd/servicehub-api
- **Último commit que representa o estado descrito:** `8594dc7a7a404a501d58bd692aedcc17a17e93d8` — "Aula 10: testes unitarios (Mockito), MockMvc e integracao" (branch `main`)
- Histórico por etapa: Checkpoint fase 1 (CRUD de Client) → Aula 8 → Aula 9 → Aula 10.

## 2. Estado das entregas

### Aula 8 · Segurança e validação — PRONTO

**O que foi feito**
- `SecurityConfig` com `SecurityFilterChain` stateless (CSRF desabilitado por ser API com token). Rotas públicas: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`. As demais exigem autenticação.
- Senhas com `BCryptPasswordEncoder` na entidade `AppUser` (coluna `password_hash`).
- Bean Validation nos DTOs:
  - `ClientRequest`: `@NotBlank`, `@Email`, `@Size` e `@Pattern` no telefone.
  - `RegisterRequest`: senha de 8 a 72 caracteres, com letras e números.
- `GlobalExceptionHandler` devolve o formato `ApiError` para 400, 401, 403, 404, 409 e 500.
- Sanitização na classe `InputSanitizer` (Apache Commons Text): trim, escape de HTML e e-mail em minúsculas. Aplicada no `ClientService` e no `AuthService`.
- `@PreAuthorize` por método no `ClientController`, com `@EnableMethodSecurity`.

**Como comprovei**
- `ClientControllerTest.post_comCamposInvalidos_retorna400ComDetalhes`: envia name vazio, email "nao-e-email" e phone "abc" para `POST /api/clients` e espera 400 com `details.email = "Informe um e-mail válido"`.
- `ClientServiceTest.create_deveSalvarClienteSanitizado_quandoEmailNaoExiste`: envia `"  <b>Ana</b>   Souza "` e `" ANA@Email.com "` e verifica que é salvo `"&lt;b&gt;Ana&lt;/b&gt; Souza"` e `"ana@email.com"`.
- `AuthFlowIntegrationTest.senhaDoAdmin_ficaArmazenadaComBCrypt`: verifica que o hash do admin começa com `$2a$` e que `passwordEncoder.matches("Admin@123", hash)` retorna true.

### Aula 9 · JWT, papéis e endpoint alternativo — PRONTO

**O que foi feito**
- `POST /api/auth/login` valida as credenciais pelo `AuthenticationManager` e devolve um JWT HS256 gerado pelo `JwtService` (jjwt 0.12), com `sub` = e-mail, claim `roles`, `iat` e `exp` de 1 hora.
- `POST /api/auth/register` cadastra PROVIDER ou CLIENT. Tentar se cadastrar como ADMIN retorna 400.
- O ADMIN inicial é criado pelo `DataInitializer`.
- O `JwtAuthFilter` (`OncePerRequestFilter`) lê o cabeçalho `Authorization: Bearer`, valida o token e preenche o `SecurityContextHolder`.
- Respostas 401 e 403 saem em JSON via `RestAuthenticationEntryPoint` e `RestAccessDeniedHandler`.
- Endpoint alternativo **GraphQL** em `POST /graphql`:
  - `schema.graphqls` com as queries `clients` e `clientById` e a mutation `createClient`.
  - Implementado no `ClientGraphQLController`, que reaproveita o `ClientService` e as mesmas regras de papel.

**Permissões por papel**

| Rota | ADMIN | PROVIDER | CLIENT | sem token |
|---|---|---|---|---|
| POST /api/clients | 201 | 403 | 403 | 401 |
| GET /api/clients | 200 | 200 | 403 | 401 |
| GET /api/clients/{id} | 200 | 200 | 200 | 401 |
| PUT / DELETE /api/clients/{id} | 200 / 204 | 403 | 403 | 401 |
| GET /api/admin/users | 200 | 403 | 403 | 401 |

**Como comprovei**
- `AuthFlowIntegrationTest.fluxoCompleto_adminCriaCliente_clientNaoPodeCriar`, em sequência:
  1. login como ADMIN → 200, com type "Bearer";
  2. `POST /api/clients` → 201;
  3. cadastro do CLIENT carlos@email.com → 201, seguido de login;
  4. `GET /api/clients/{id}` → 200;
  5. `POST /api/clients` → 403;
  6. `GET /api/admin/users` → 403.
- `login_comSenhaErrada_retorna401`: 401 com a mensagem "E-mail ou senha inválidos.".
- `rotaProtegida_comTokenAdulterado_retorna401`: 401.
- `JwtServiceTest`: cobre token válido, adulterado e expirado.
- GraphQL:
  - `graphql_listaClientes_comTokenAdmin`: `{ clients { id name email } }` retorna os dados, e o campo `phone` não vem na resposta.
  - `graphql_mutationCreateClient_comPapelClient_retornaErroForbidden`: erro FORBIDDEN, nenhum cliente criado.
  - `graphql_semToken_retorna401`: 401.

### Aula 10 · Testes — PRONTO

**O que foi feito** (29 testes em 6 classes)

| Classe | Tipo | Testes |
|---|---|---|
| ClientServiceTest | Unitário (Mockito) | 5: sanitização, 409 no create e no update, 404, delete |
| AuthServiceTest | Unitário (Mockito) | 5: recusa de ADMIN, senha salva como hash, 409, login com papel, BadCredentials |
| JwtServiceTest | Unitário | 3: token válido, adulterado e expirado |
| ClientControllerTest | MockMvc (`@WebMvcTest` + SecurityConfig real + `@MockitoBean`) | 7: 201 com Location, 400, 401, 403, 200, 404 |
| AuthFlowIntegrationTest | Integração (`@SpringBootTest` + H2) | 8: login → token → rotas por papel → GraphQL |
| ServicehubApiApplicationTests | Contexto | 1: contextLoads |

**Como comprovei:** execução de `mvn -B test` no GitHub Actions (aba Actions do repositório).

## 3. Decisão técnica e justificativa

Escolhemos **GraphQL em vez de gRPC** como endpoint alternativo.

- **Por que não gRPC:** exigiria uma segunda porta (9090), plugin de compilação do `.proto`, stubs gerados e um interceptor próprio para validar o JWT, ou seja, duplicar a camada de segurança.
- **Por que GraphQL:** roda no mesmo servidor HTTP (porta 8080, rota `/graphql`). Por isso:
  - passa pela mesma `SecurityFilterChain` e pelo mesmo `JwtAuthFilter`;
  - aceita as mesmas anotações `@PreAuthorize`;
  - chama o mesmo `ClientService`, herdando validação, sanitização e o 409 de e-mail duplicado sem reescrever nada.
- **Ganho extra:** resolve o over-fetching, como mostra o teste `graphql_listaClientes_comTokenAdmin`.
- Como o ServiceHub ainda é um monólito, a vantagem de performance do gRPC não compensava o custo.

## 4. Dificuldade ou erro e como resolvemos

**(a) Erro em código gerado por IA**
- **O problema:** a versão sugerida do `GlobalExceptionHandler` tinha um `@ExceptionHandler(Exception.class)` genérico. Quando o `@PreAuthorize` nega acesso, o Spring Security 6 lança `AuthorizationDeniedException` (subclasse de `AccessDeniedException`) dentro do controller. O handler genérico capturaria a exceção e um CLIENT tentando `POST /api/clients` receberia 500 em vez de 403.
- **A correção:**
  - handler específico para `AccessDeniedException` → 403;
  - handler para `AuthenticationException` → 401 (login com senha errada);
  - o handler genérico passou a preservar o status de exceções do próprio Spring (`ErrorResponse`, como o 404 de rota inexistente).
- **Garantia:** os testes `post_comPapelClient_retorna403` e `delete_comProvider_retorna403` impedem que o erro volte.

**(b) Ambiente**
- **O problema:** no ambiente de desenvolvimento não foi possível baixar as dependências do Maven Central, então os testes não rodavam localmente.
- **A solução:** os testes passaram a ser executados com `mvn -B test` no GitHub Actions.

## 5. Próximos passos (em ordem de prioridade)

1. Workflow de CI permanente para rodar `mvn test` em todo push e pull request.
2. Implementar as entidades Provider, ServiceContract e Review, ligando Provider e Client ao `AppUser`, para que um CLIENT só veja os próprios dados.
3. Expandir o schema GraphQL com os tipos Provider, Service e Booking e a mutation `bookService`.
4. Segurança do token: refresh token, revogação no logout e tirar o segredo JWT e a senha padrão do admin do `application.properties`.
5. Testes com PostgreSQL real (Testcontainers), índices nas colunas de busca (Aula 11) e a API no docker-compose junto com o banco (Aula 12).
