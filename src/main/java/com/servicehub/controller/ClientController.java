package com.servicehub.controller;

import com.servicehub.dto.ClientRequest;
import com.servicehub.dto.ClientResponse;
import com.servicehub.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clientes", description = "Operações de cadastro e gerenciamento de clientes do ServiceHub")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar cliente",
            description = "Cria um novo cliente no ServiceHub. O e-mail deve ser único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    public ResponseEntity<ClientResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do cliente",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ClientRequest.class),
                            examples = @ExampleObject(
                                    name = "Cliente exemplo",
                                    value = "{\"name\":\"Bernardo Faria\",\"email\":\"bernardo@email.com\",\"phone\":\"(62) 99999-9999\"}"
                            )
                    )
            )
            @Valid @RequestBody ClientRequest request) {

        ClientResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/clients/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar clientes",
            description = "Retorna todos os clientes cadastrados no ServiceHub."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<ClientResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar cliente por ID",
            description = "Retorna os dados de um cliente a partir do identificador informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClientResponse> findById(
            @PathVariable @Schema(description = "ID do cliente", example = "1") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar cliente",
            description = "Substitui os dados do cliente identificado pelo ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado em outro cliente")
    })
    public ResponseEntity<ClientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir cliente",
            description = "Remove permanentemente o cliente identificado pelo ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
