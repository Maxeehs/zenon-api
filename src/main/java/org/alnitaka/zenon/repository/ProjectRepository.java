package org.alnitaka.zenon.repository;

import java.util.List;
import java.util.Optional;
import org.alnitaka.zenon.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
	// Récupère tous les projets de cet utilisateur
	List<Project> findByOwnerEmail(String ownerEmail);

	// Pour update/delete, charger en une fois
	Optional<Project> findByIdAndOwnerId(Long id, Long ownerId);
}
