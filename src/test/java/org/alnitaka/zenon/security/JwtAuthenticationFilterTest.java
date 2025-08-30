package org.alnitaka.zenon.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
	@Mock
	JwtTokenProvider tokenProvider;
	@Mock
	UserDetailsService userDetailsService;
	@Mock
	HttpServletRequest request;
	@Mock
	HttpServletResponse response;
	@Mock
	FilterChain chain;

	JwtAuthenticationFilter filter;

	@BeforeEach
	void setUp() {
		filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilterInternal_doesNothing_whenNoAuthorizationHeader() throws ServletException, IOException {
		// Arrange
		when(request.getHeader("Authorization")).thenReturn(null);

		// Act
		filter.doFilterInternal(request, response, chain);

		// Assert
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(chain).doFilter(request, response);
		verifyNoInteractions(tokenProvider, userDetailsService);
	}

	@Test
	void doFilterInternal_doesNothing_whenHeaderIsNotBearer() throws ServletException, IOException {
		// Arrange
		when(request.getHeader("Authorization")).thenReturn("Basic abc123");

		// Act
		filter.doFilterInternal(request, response, chain);

		// Assert
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(chain).doFilter(request, response);
		verifyNoInteractions(tokenProvider, userDetailsService);
	}

	@Test
	void doFilterInternal_doesNothing_whenTokenInvalid() throws ServletException, IOException {
		// Arrange
		when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
		when(tokenProvider.validateToken("bad.token")).thenReturn(false);

		// Act
		filter.doFilterInternal(request, response, chain);

		// Assert
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(tokenProvider).validateToken("bad.token");
		verify(tokenProvider, never()).getUsernameFromJwt(anyString());
		verifyNoInteractions(userDetailsService);
		verify(chain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_setsAuthentication_whenTokenValid() throws ServletException, IOException {
		// Arrange
		when(request.getHeader("Authorization")).thenReturn("Bearer good.token");
		when(tokenProvider.validateToken("good.token")).thenReturn(true);
		when(tokenProvider.getUsernameFromJwt("good.token")).thenReturn("alice@example.com");

		UserDetails user = User.withUsername("alice@example.com")
			.password("x")
			.authorities("ROLE_USER")
			.build();
		when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(user);

		// Optionnel : pour que WebAuthenticationDetailsSource construise des détails,
		// certains systèmes consultent des getters du request ; pas obligatoire ici.
		when(request.getRemoteAddr()).thenReturn("127.0.0.1");

		// Act
		filter.doFilterInternal(request, response, chain);

		// Assert
		var auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.isAuthenticated()).isTrue();
		assertThat(auth.getPrincipal()).isEqualTo(user);
		assertThat(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
			.containsExactlyInAnyOrderElementsOf(
				user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
			);
		assertThat(auth.getDetails()).isNotNull(); // détails construits depuis la requête

		verify(tokenProvider).validateToken("good.token");
		verify(tokenProvider).getUsernameFromJwt("good.token");
		verify(userDetailsService).loadUserByUsername("alice@example.com");
		verify(chain).doFilter(request, response);
		verifyNoMoreInteractions(tokenProvider, userDetailsService);
	}
}
