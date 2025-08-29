package org.alnitaka.zenon.service;

import java.util.Optional;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@Mock
	UserRepository userRepository;
	@InjectMocks
	UserService service;

	@BeforeEach
	void clearBefore() {
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void clearAfter() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getCurrentUser_returnsEmpty_whenNoAuthenticationInContext() {
		// Arrange
		SecurityContextHolder.clearContext();

		// Act
		Optional<User> result = service.getCurrentUser();

		// Assert
		assertThat(result).isEmpty();
		verifyNoInteractions(userRepository);
	}

	@Test
	void getCurrentUser_returnsEmpty_whenNotAuthenticated() {
		// Arrange: authenticated = false
		UserDetails principal = org.springframework.security.core.userdetails.User
			.withUsername("john@example.com").password("x").authorities("ROLE_USER").build();
		var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		auth.setAuthenticated(false); // force faux
		SecurityContextHolder.getContext().setAuthentication(auth);

		// Act
		Optional<User> result = service.getCurrentUser();

		// Assert
		assertThat(result).isEmpty();
		verifyNoInteractions(userRepository);
	}

	@Test
	void getCurrentUser_returnsEmpty_whenPrincipalIsString() {
		// Arrange: simulate anonymousUser (principal String)
		var auth = new UsernamePasswordAuthenticationToken("anonymousUser", null, null);
		SecurityContextHolder.getContext().setAuthentication(auth);

		// Act
		Optional<User> result = service.getCurrentUser();

		// Assert
		assertThat(result).isEmpty();
		verifyNoInteractions(userRepository);
	}

	@Test
	void getCurrentUser_returnsUser_whenUserDetailsAndFoundInRepo() {
		// Arrange
		String email = "jane@example.com";
		UserDetails principal = org.springframework.security.core.userdetails.User
			.withUsername(email).password("x").authorities("ROLE_USER").build();
		var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);

		User jane = new User();
		jane.setId(123L);
		when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(jane));

		// Act
		Optional<User> result = service.getCurrentUser();

		// Assert
		assertThat(result).containsSame(jane);
		verify(userRepository).findUserByEmail(email);
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	void getCurrentUser_returnsEmpty_whenUserDetailsButRepoNotFound() {
		// Arrange
		String email = "ghost@example.com";
		UserDetails principal = org.springframework.security.core.userdetails.User
			.withUsername(email).password("x").authorities("ROLE_USER").build();
		var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);

		when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

		// Act
		Optional<User> result = service.getCurrentUser();

		// Assert
		assertThat(result).isEmpty();
		verify(userRepository).findUserByEmail(email);
		verifyNoMoreInteractions(userRepository);
	}
}
