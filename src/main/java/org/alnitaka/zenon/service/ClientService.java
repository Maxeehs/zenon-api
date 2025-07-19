package org.alnitaka.zenon.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.alnitaka.zenon.entity.Client;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.repository.ClientRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ClientService {
	private final ClientRepository clientRepo;
	private final UserService userService;
	private static final String NO_AUTH = "Non authentifié";

	public List<Client> listMyClients() {
		String email = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH))
			.getEmail();
		return clientRepo.findByOwnerEmail(email);
	}

	public Client getClient(Long id) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		return clientRepo.findByIdAndOwnerId(id, me.getId())
			.orElseThrow(() -> new EntityNotFoundException("Client introuvable"));
	}

	public Client create(Client dto) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		dto.setOwner(me);
		return clientRepo.save(dto);
	}

	public Client update(Long id, Client update) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		Client existing = clientRepo.findByIdAndOwnerId(id, me.getId())
			.orElseThrow(() -> new EntityNotFoundException("Client introuvable"));
		// Appliquer les changements voulus
		existing.setNom(update.getNom());
		existing.setEmail(update.getEmail());
		// …
		return clientRepo.save(existing);
	}

	public void delete(Long id) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		Client existing = clientRepo.findByIdAndOwnerId(id, me.getId())
			.orElseThrow(() -> new EntityNotFoundException("Client introuvable"));
		clientRepo.delete(existing);
	}
}
