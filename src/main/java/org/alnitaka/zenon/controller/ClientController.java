package org.alnitaka.zenon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.alnitaka.zenon.dto.ClientDto;
import org.alnitaka.zenon.mapper.ClientMapper;
import org.alnitaka.zenon.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Client")
@RequiredArgsConstructor
@RequestMapping("/api/clients")
public class ClientController {
	private final ClientService clientService;
	private final ClientMapper clientMapper;

	@GetMapping
	@Operation(summary = "Liste les clients de l'utilisateur courant")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Operation successful",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
				array = @ArraySchema(schema = @Schema(implementation = ClientDto.class))
			)
		)
	})
	public List<ClientDto> getMyClients() {
		return clientMapper.toDto(clientService.listMyClients());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Récupère un client par son ID")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Operation successful",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClientDto.class))
		)
	})
	public ClientDto getMyClients(@PathVariable Long id) {
		return clientMapper.toDto(clientService.getClient(id));
	}

	@PostMapping
	@Operation(summary = "Crée un nouveau client")
	public ResponseEntity<ClientDto> create(@RequestBody @Valid ClientDto client) {
		ClientDto saved = clientMapper.toDto(clientService.create(client));
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@PutMapping
	@Operation(summary = "Met à jour un client existant")
	public ClientDto update(@RequestBody @Valid ClientDto client) {
		return clientMapper.toDto(clientService.update(client));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Supprime un client par son ID")
	public void delete(@PathVariable Long id) {
		clientService.delete(id);
	}
}
