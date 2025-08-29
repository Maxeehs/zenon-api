package org.alnitaka.zenon.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.alnitaka.zenon.dto.ClientDto;
import org.alnitaka.zenon.entity.Client;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.mapper.ClientMapper;
import org.alnitaka.zenon.repository.ClientRepository;
import org.alnitaka.zenon.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {
	private final ClientRepository clientRepo;
	private final UserRepository userRepository;
	private final UserService userService;
	private final ClientMapper clientMapper;
	private static final String NO_AUTH = "Non authentifié";
	private static final String NO_CLIENT = "Client introuvable";

	/**
	 * Retrieves all {@link Client} entities owned by the currently authenticated user.
	 * <p>
	 * The method obtains the current user's email via {@link UserService#getCurrentUser()}
	 * and then queries {@link ClientRepository} for clients with that owner email.
	 *
	 * @return a {@link List} of {@link Client} instances belonging to the authenticated user.
	 * @throws AccessDeniedException if no authenticated user is present.
	 */
	public List<Client> listMyClients() {
		String email = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH))
			.getEmail();
		return clientRepo.findByOwnerEmail(email);
	}

	/**
	 * Retrieves a {@link Client} entity identified by the specified {@code id} that belongs to the currently
	 * authenticated user.
	 *
	 * @param id the unique identifier of the client to retrieve
	 * @return the {@link Client} instance associated with the given {@code id} and owned by the current user
	 * @throws AccessDeniedException if there is no authenticated user in the security context
	 * @throws EntityNotFoundException if no client with the specified {@code id} exists for the current user
	 */
	public Client getClient(Long id) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		return clientRepo.findByIdAndOwnerId(id, me.getId())
			.orElseThrow(() -> new EntityNotFoundException(NO_CLIENT));
	}

	/**
	 * Creates a new {@link Client} entity from the supplied {@link ClientDto}.
	 *
	 * @param dto the DTO containing the client data
	 * @return the persisted {@link Client} instance
	 */
	public Client create(ClientDto dto) {
		Client newUser = clientMapper.toEntity(dto);
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		newUser.setOwner(me);
		return clientRepo.save(newUser);
	}

	/**
	 * Updates the client identified by {@code id} with the values from the {@code update}
	 * instance. Only the {@code nom} and {@code email} fields of the existing client are
	 * modified. The method ensures that the client belongs to the currently authenticated
	 * user and persists the changes via {@link ClientRepository}.
	 *
	 * @param id     the unique identifier of the client to update
	 * @param update a {@link Client} containing the new {@code nom} and {@code email}
	 * @return the updated client represented as a {@link ClientDto}
	 * @throws AccessDeniedException if no user is currently authenticated
	 * @throws EntityNotFoundException if a client with the given {@code id} does not exist
	 *                                 for the current user
	 */
	public Client update(ClientDto dto) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		Client existing = clientRepo.findByIdAndOwnerId(dto.id(), me.getId())
			.orElseThrow(() -> new EntityNotFoundException(NO_CLIENT));
		User owner = existing.getOwner();
		// Appliquer les changements voulus
		existing.setNom(dto.nom());
		existing.setEmail(dto.email());
		if (
			owner != null
			&& dto.owner() != null
			&& !dto.owner().id().equals(owner.getId())
		) {
			User newUser = userRepository.findById(dto.owner().id()).orElseThrow();
			existing.setOwner(newUser);
		}
		return clientRepo.save(existing);
	}

	/**
	 * Deletes the client identified by the given {@code id} belonging to the
	 * currently authenticated user.
	 *
	 * @param id the identifier of the client to delete
	 */
	public void delete(Long id) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		Client existing = clientRepo.findByIdAndOwnerId(id, me.getId())
			.orElseThrow(() -> new EntityNotFoundException(NO_CLIENT));
		clientRepo.delete(existing);
	}
}
