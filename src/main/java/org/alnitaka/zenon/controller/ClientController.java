package org.alnitaka.zenon.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.alnitaka.zenon.entity.Client;
import org.alnitaka.zenon.service.ClientService;
import org.springframework.http.HttpStatus;
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

	@GetMapping
	public List<Client> getMyClients() {
		return clientService.listMyClients();
	}

	@GetMapping("/{id}")
	public Client getMyClients(@PathVariable Long id) {
		return clientService.getClient(id);
	}

	@PostMapping
	public ResponseEntity<Client> create(@RequestBody @Valid Client client) {
		Client saved = clientService.create(client);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@PutMapping("/{id}")
	public Client update(@PathVariable Long id,
						 @RequestBody @Valid Client client) {
		return clientService.update(id, client);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		clientService.delete(id);
	}
}
