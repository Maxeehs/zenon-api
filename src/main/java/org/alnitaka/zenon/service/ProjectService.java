package org.alnitaka.zenon.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.alnitaka.zenon.entity.Project;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.repository.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProjectService {
	private final ProjectRepository projectRepo;
	private final UserService userService;
	private static final String NO_AUTH = "Non authentifié";

	public List<Project> listProjects() {
		String email = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH))
			.getEmail();
		return projectRepo.findByOwnerEmail(email);
	}

	public Project getProject(Long id) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		return projectRepo.findByIdAndOwnerId(id, me.getId())
			.orElseThrow(() -> new EntityNotFoundException("Projet introuvable"));
	}

	public Project create(Project newProject) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		newProject.setOwner(me);
		return projectRepo.save(newProject);
	}

	public Project update(Long id, Project updatedProject) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		Project existing = projectRepo.findByIdAndOwnerId(id, me.getId())
			.orElseThrow(() -> new EntityNotFoundException("Projet introuvable"));
		// Appliquer les changements voulus
		existing.setNom(updatedProject.getNom());
		// …
		return projectRepo.save(existing);
	}

	public void delete(Long id) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		Project existing = projectRepo.findByIdAndOwnerId(id, me.getId())
			.orElseThrow(() -> new EntityNotFoundException("Projet introuvable"));
		projectRepo.delete(existing);
	}
}
