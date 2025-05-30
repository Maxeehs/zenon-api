package org.alnitaka.zenon.repository;

import java.util.Optional;
import org.alnitaka.zenon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findUserByEmail(String email);
}
