package org.alnitaka.zenon.repository;

import java.util.List;
import java.util.Optional;
import org.alnitaka.zenon.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
	// Récupère tous les clients de cet utilisateur
	List<Client> findByOwnerEmail(String email);

	// Pour update/delete, charger en une fois
	Optional<Client> findByIdAndOwnerId(Long id, Long ownerId);
}
