package org.alnitaka.zenon.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.alnitaka.zenon.dto.ProjectDto;
import org.alnitaka.zenon.entity.Client;
import org.alnitaka.zenon.entity.Project;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.mapper.ProjectMapper;
import org.alnitaka.zenon.repository.ClientRepository;
import org.alnitaka.zenon.repository.ProjectRepository;
import org.alnitaka.zenon.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {
	private final ProjectRepository projectRepo;
	private final UserRepository userRepository;
	private final ClientRepository clientRepository;
	private final ProjectMapper projectMapper;
	private final UserService userService;
	private static final String NO_AUTH = "Non authentifié";
	private static final String NO_PROJECT = "Projet introuvable";

	/**
	 * Retrieves all projects owned by the currently authenticated user.
	 * <p>
	 * The method obtains the current user's email via {@link UserService#getCurrentUser()}.
	 * If no user is authenticated, an {@link AccessDeniedException} is thrown.
	 * Projects are fetched from {@link ProjectRepository#findByOwnerEmail(String)}.
	 *
	 * @return a list of {@link Project} instances belonging to the current user
	 */
	public List<Project> listProjects() {
		String email = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH))
			.getEmail();
		return projectRepo.findByOwnerEmail(email);
	}

	/**
	 * Returns the {@link Project} identified by the given {@code id} that is owned by the
	 * currently authenticated user.
	 *
	 * @param id the identifier of the project to retrieve
	 * @return the {@link Project} owned by the current user with the specified {@code id}
	 * @throws AccessDeniedException if no user is authenticated
	 * @throws EntityNotFoundException if the project with the specified {@code id} does not exist for the current user
	 */
	public Project getProject(Long id) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		return projectRepo.findByIdAndOwnerId(id, me.getId())
			.orElseThrow(() -> new EntityNotFoundException(NO_PROJECT));
	}

	/**
	 * Creates and persists a new {@link Project} based on the supplied {@link ProjectDto}.
	 *
	 * @param dto the data transfer object containing the project details
	 * @return the persisted {@link Project} instance with an assigned identifier and owner
	 * @throws AccessDeniedException if no authenticated user is found
	 */
	public Project create(ProjectDto dto) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		Project newProject = projectMapper.toEntity(dto);
		newProject.setOwner(me);

		if (dto.client()!= null && dto.client().id() != null) {
			Client newClient = clientRepository.findById(dto.client().id()).orElseThrow();
			newProject.setClient(newClient);
		}
		return projectRepo.save(newProject);
	}

	/**
	 * Updates an existing {@link Project} identified by the specified {@code id}
	 * with the data provided in {@link ProjectDto}. The method verifies that the
	 * current user is authenticated and owns the project; if not, an
	 * {@link AccessDeniedException} is thrown. The project’s name is set to
	 * {@code dto.nom()}. If the owner or client referenced in {@code dto} differs
	 * from the current values, the corresponding {@link User} or {@link Client}
	 * entities are retrieved and assigned. The updated project is persisted and
	 * returned.
	 *
	 * @param dto  the data transfer object containing the new values
	 * @return the updated {@link Project} instance
	 */
	public Project update(ProjectDto dto) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		Project existing = projectRepo.findByIdAndOwnerId(dto.id(), me.getId())
			.orElseThrow(() -> new EntityNotFoundException(NO_PROJECT));
		// Appliquer les changements voulus
		existing.setNom(dto.nom());
		if (
			dto.owner() != null
			&& existing.getOwner() != null
			&& !dto.owner().id().equals(existing.getOwner().getId())
		) {
			User newUser = userRepository.findById(dto.owner().id()).orElseThrow();
			existing.setOwner(newUser);
		}
		if (dto.client() != null) {
			Client newClient = clientRepository.findById(dto.client().id()).orElseThrow();
			existing.setClient(newClient);
		} else {
			existing.setClient(null);
		}
		return projectRepo.save(existing);
	}

	/**
	 * Deletes the project with the specified {@code id} that belongs to the currently authenticated user.
	 * The method first retrieves the current user via {@link UserService#getCurrentUser()}. If no user is
	 * authenticated, an {@link AccessDeniedException} is thrown. It then attempts to locate the project
	 * by {@code id} and the current user’s identifier; if the project does not exist for this user,
	 * an {@link EntityNotFoundException} is*/
	public void delete(Long id) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		Project existing = projectRepo.findByIdAndOwnerId(id, me.getId())
			.orElseThrow(() -> new EntityNotFoundException(NO_PROJECT));
		projectRepo.delete(existing);
	}
}
