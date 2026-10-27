package com.servicehub.service;

import com.servicehub.dto.ClientRequest;
import com.servicehub.dto.ClientResponse;
import com.servicehub.exception.EmailAlreadyExistsException;
import com.servicehub.exception.ResourceNotFoundException;
import com.servicehub.model.Client;
import com.servicehub.repository.ClientRepository;
import com.servicehub.util.InputSanitizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Testes unitários do ClientService: o repositório é mockado com Mockito (sem Spring, sem banco). */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository repository;

    @Spy
    private InputSanitizer sanitizer = new InputSanitizer();

    @InjectMocks
    private ClientService service;

    @Test
    void create_deveSalvarClienteSanitizado_quandoEmailNaoExiste() {
        ClientRequest request = new ClientRequest("  <b>Ana</b>   Souza ", " ANA@Email.com ", "(62) 99999-9999");
        when(repository.existsByEmail("ana@email.com")).thenReturn(false);
        when(repository.save(any(Client.class))).thenAnswer(inv -> {
            Client c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        ClientResponse response = service.create(request);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("&lt;b&gt;Ana&lt;/b&gt; Souza");
        assertThat(captor.getValue().getEmail()).isEqualTo("ana@email.com");
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void create_deveLancarConflito_quandoEmailJaExiste() {
        ClientRequest request = new ClientRequest("Ana", "ana@email.com", "(62) 99999-9999");
        when(repository.existsByEmail("ana@email.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void findById_deveLancarNotFound_quandoIdNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
        assertThat(ex.getMessage()).isEqualTo("Cliente com ID 99 não encontrado.");
    }

    @Test
    void update_deveLancarConflito_quandoEmailPertenceAOutroCliente() {
        Client existente = new Client(1L, "Ana", "ana@email.com", "(62) 99999-9999");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByEmailAndIdNot("bia@email.com", 1L)).thenReturn(true);

        ClientRequest request = new ClientRequest("Ana", "bia@email.com", "(62) 99999-9999");

        assertThrows(EmailAlreadyExistsException.class, () -> service.update(1L, request));
        verify(repository, never()).save(any());
    }

    @Test
    void delete_deveRemoverCliente_quandoIdExiste() {
        Client existente = new Client(1L, "Ana", "ana@email.com", "(62) 99999-9999");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.delete(1L);

        verify(repository).delete(existente);
    }
}
