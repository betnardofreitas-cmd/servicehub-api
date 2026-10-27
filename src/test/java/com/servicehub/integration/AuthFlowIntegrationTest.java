package com.servicehub.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicehub.repository.ClientRepository;
import com.servicehub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de integração: sobe a aplicação inteira (Spring Security + JWT + JPA + H2 em memória)
 * e percorre o fluxo real: login -> token -> rotas protegidas por papel -> GraphQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private ClientRepository clientRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparClientes() {
        clientRepository.deleteAll();
    }

    @Test
    void fluxoCompleto_adminCriaCliente_clientNaoPodeCriar() throws Exception {
        String adminToken = login("admin@servicehub.com", "Admin@123");

        // ADMIN cria cliente -> 201
        MvcResult created = mockMvc.perform(post("/api/clients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"<script>x</script>Ana\",\"email\":\"ANA@email.com\",\"phone\":\"(62) 99999-9999\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("&lt;script&gt;x&lt;/script&gt;Ana")) // sanitizado
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andReturn();
        long id = mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // usuário CLIENT se cadastra e faz login
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Carlos\",\"email\":\"carlos@email.com\",\"password\":\"Senha123\",\"role\":\"CLIENT\"}"))
                .andExpect(status().isCreated());
        String clientToken = login("carlos@email.com", "Senha123");

        // CLIENT pode consultar por ID -> 200
        mockMvc.perform(get("/api/clients/" + id).header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk());

        // CLIENT não pode criar -> 403
        mockMvc.perform(post("/api/clients")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bia\",\"email\":\"bia@email.com\",\"phone\":\"(62) 98888-8888\"}"))
                .andExpect(status().isForbidden());

        // CLIENT não pode listar usuários (rota de ADMIN) -> 403
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_comSenhaErrada_retorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@servicehub.com\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));
    }

    @Test
    void rotaProtegida_comTokenAdulterado_retorna401() throws Exception {
        String token = login("admin@servicehub.com", "Admin@123");

        mockMvc.perform(get("/api/clients").header("Authorization", "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_naoPermiteAutoCadastroComoAdmin() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mal\",\"email\":\"mal@email.com\",\"password\":\"Senha123\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void senhaDoAdmin_ficaArmazenadaComBCrypt() {
        String hash = userRepository.findByEmail("admin@servicehub.com").orElseThrow().getPasswordHash();

        assertThat(hash).startsWith("$2a$").isNotEqualTo("Admin@123");
        assertThat(passwordEncoder.matches("Admin@123", hash)).isTrue();
    }

    @Test
    void graphql_listaClientes_comTokenAdmin() throws Exception {
        String adminToken = login("admin@servicehub.com", "Admin@123");
        mockMvc.perform(post("/api/clients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@email.com\",\"phone\":\"(62) 99999-9999\"}"))
                .andExpect(status().isCreated());

        JsonNode body = graphql(adminToken, "{ clients { id name email } }");

        assertThat(body.at("/data/clients/0/email").asText()).isEqualTo("ana@email.com");
        assertThat(body.at("/data/clients/0/phone").isMissingNode()).isTrue(); // só vem o que foi pedido
    }

    @Test
    void graphql_mutationCreateClient_comPapelClient_retornaErroForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dani\",\"email\":\"dani@email.com\",\"password\":\"Senha123\",\"role\":\"CLIENT\"}"))
                .andExpect(status().isCreated());
        String clientToken = login("dani@email.com", "Senha123");

        JsonNode body = graphql(clientToken,
                "mutation { createClient(input: {name: \\\"X\\\", email: \\\"x@email.com\\\", phone: \\\"(62) 90000-0000\\\"}) { id } }");

        assertThat(body.at("/errors/0/extensions/classification").asText()).isEqualTo("FORBIDDEN");
        assertThat(clientRepository.count()).isZero();
    }

    @Test
    void graphql_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/graphql").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{ clients { id } }\"}"))
                .andExpect(status().isUnauthorized());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    /** O handler HTTP do Spring GraphQL pode responder de forma assíncrona; nesse caso fazemos o asyncDispatch. */
    private JsonNode graphql(String token, String query) throws Exception {
        MvcResult result = mockMvc.perform(post("/graphql")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"" + query + "\"}"))
                .andReturn();
        if (result.getRequest().isAsyncStarted()) {
            result = mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk()).andReturn();
        }
        return mapper.readTree(result.getResponse().getContentAsString());
    }
}
