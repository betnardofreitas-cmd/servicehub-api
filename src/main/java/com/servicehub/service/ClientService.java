package com.servicehub.service;

import com.servicehub.dto.ClientRequest;
import com.servicehub.dto.ClientResponse;
import com.servicehub.exception.EmailAlreadyExistsException;
import com.servicehub.exception.ResourceNotFoundException;
import com.servicehub.model.Client;
import com.servicehub.repository.ClientRepository;
import com.servicehub.util.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository repository;
    private final InputSanitizer sanitizer;

    public ClientService(ClientRepository repository, InputSanitizer sanitizer) {
        this.repository = repository;
        this.sanitizer = sanitizer;
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        String email = sanitizer.email(request.getEmail());
        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Já existe um cliente cadastrado com este e-mail.");
        }

        Client client = new Client();
        applySanitized(client, request);

        return toResponse(repository.save(client));
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = findEntity(id);

        if (repository.existsByEmailAndIdNot(sanitizer.email(request.getEmail()), id)) {
            throw new EmailAlreadyExistsException("Já existe outro cliente cadastrado com este e-mail.");
        }

        applySanitized(client, request);

        return toResponse(repository.save(client));
    }

    @Transactional
    public void delete(Long id) {
        Client client = findEntity(id);
        repository.delete(client);
    }

    /** Sanitização na camada de service (Aula 8): trim, escape de HTML, e-mail em minúsculas. */
    private void applySanitized(Client client, ClientRequest request) {
        client.setName(sanitizer.text(request.getName()));
        client.setEmail(sanitizer.email(request.getEmail()));
        client.setPhone(sanitizer.phone(request.getPhone()));
    }

    private Client findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente com ID " + id + " não encontrado."
                ));
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(client.getId(), client.getName(), client.getEmail(), client.getPhone());
    }
}
