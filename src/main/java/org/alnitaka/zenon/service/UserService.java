package org.alnitaka.zenon.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;

	public Optional<User> getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
			// Pas d’utilisateur authentifié
			return Optional.empty();
		}

		String email = ((UserDetails) auth.getPrincipal()).getUsername();
		return userRepository.findUserByEmail(email);
	}
}
