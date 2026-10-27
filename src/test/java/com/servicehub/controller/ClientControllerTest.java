package com.servicehub.controller;

import com.servicehub.config.SecurityConfig;
import com.servicehub.dto.ClientRequest;
import com.servicehub.dto.ClientResponse;
import com.servicehub.exception.ResourceNotFoundException;
import com.servicehub.security.JwtService;
import com.servicehub.security.RestAccessDeniedHandler;
import com.servicehub.security.RestAuthenticationEntryPoint;
import com.servicehub.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes do controller com MockMvc (camada web + Spring Security reais, service mockado).
 * Verifica status HTTP, JSON de resposta, validação e regras de papel (@PreAuthorize).
 */
@WebMvcTest(ClientController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class ClientControllerTest {

    private static final String VALID_JSON =
            "{\"name\":\"Ana Souza\",\"email\":\"ana@email.com\",\"phone\":\"(62) 99999-9999\"}";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ClientService clientService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void post_comAdmin_retorna201ELocation() throws Exception {
        when(clientService.create(any(ClientRequest.class)))
                .thenReturn(new ClientResponse(1L, "Ana Souza", "ana@email.com", "(62) 99999-9999"));

        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clients/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana@email.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void post_comCamposInvalidos_retorna400ComDetalhes() throws Exception {
        String invalido = "{\"name\":\"\",\"email\":\"nao-e-email\",\"phone\":\"abc\"}";

        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details.email").value("Informe um e-mail válido"))
                .andExpect(jsonPath("$.details.phone").exists());

        verifyNoInteractions(clientService);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void post_comPapelClient_retorna403() throws Exception {
        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(clientService, never()).create(any());
    }

    @Test
    void get_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    void list_comProvider_retorna200() throws Exception {
        when(clientService.findAll())
                .thenReturn(List.of(new ClientResponse(1L, "Ana Souza", "ana@email.com", "(62) 99999-9999")));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    void getById_inexistente_retorna404() throws Exception {
        when(clientService.findById(99L)).thenThrow(new ResourceNotFoundException("Cliente com ID 99 não encontrado."));

        mockMvc.perform(get("/api/clients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente com ID 99 não encontrado."));
    }

    @Test
    @WithMockUser(roles = "PROVIDER")
    void delete_comProvider_retorna403() throws Exception {
        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isForbidden());

        verify(clientService, never()).delete(any());
    }
}
