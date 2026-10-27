package com.servicehub.graphql;

import com.servicehub.dto.ClientRequest;
import com.servicehub.dto.ClientResponse;
import com.servicehub.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Endpoint alternativo GraphQL que reaproveita o MESMO ClientService do REST
 * (mesma validação, sanitização e regras de negócio) e as MESMAS regras de papel.
 */
@Controller
public class ClientGraphQLController {

    private final ClientService clientService;

    public ClientGraphQLController(ClientService clientService) {
        this.clientService = clientService;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER')")
    public List<ClientResponse> clients() {
        return clientService.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','CLIENT')")
    public ClientResponse clientById(@Argument Long id) {
        return clientService.findById(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ClientResponse createClient(@Argument @Valid ClientRequest input) {
        return clientService.create(input);
    }
}
