package com.servicehub.service;

import com.servicehub.dto.ClientRequest;
import com.servicehub.dto.ClientResponse;
import com.servicehub.exception.EmailAlreadyExistsException;
import com.servicehub.exception.ResourceNotFoundException;
import com.servicehub.model.Client;
import com.servicehub.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Já existe um cliente cadastrado com este e-mail.");
        }

        Client client = new Client();
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());

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

        if (repository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new EmailAlreadyExistsException("Já existe outro cliente cadastrado com este e-mail.");
        }

        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());

        return toResponse(repository.save(client));
    }

    @Transactional
    public void delete(Long id) {
        Client client = findEntity(id);
        repository.delete(client);
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
