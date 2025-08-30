package org.alnitaka.zenon.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {
	private JwtTokenProvider provider;
	private static final String SECRET_64B =
		// 64 chars (>= 64 bytes) -> OK pour HS512
		"0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";

	@BeforeEach
	void setUp() {
		provider = new JwtTokenProvider();
		ReflectionTestUtils.setField(provider, "jwtSecret", SECRET_64B);
		// 1 heure
		ReflectionTestUtils.setField(provider, "jwtExpirationMs", 3_600_000L);
	}

	private Authentication authWithName(String name) {
		// Simple Authentication simulée
		return new UsernamePasswordAuthenticationToken(name, null, null);
	}

	@Test
	void generateToken_and_getUsername_roundTrip() {
		Authentication auth = authWithName("alice@example.com");

		String token = provider.generateToken(auth);
		assertThat(token).isNotBlank();

		String username = provider.getUsernameFromJwt(token);
		assertThat(username).isEqualTo("alice@example.com");
	}

	@Test
	void validateToken_returnsTrue_forValidToken() {
		String token = provider.generateToken(authWithName("bob@example.com"));
		assertThat(provider.validateToken(token)).isTrue();
	}

	@Test
	void validateToken_returnsFalse_forMalformedToken() {
		String malformed = "not.a.jwt";
		assertThat(provider.validateToken(malformed)).isFalse();
	}

	@Test
	void validateToken_returnsFalse_forExpiredToken() {
		// Génère un token déjà expiré
		ReflectionTestUtils.setField(provider, "jwtExpirationMs", -1_000L);
		String expired = provider.generateToken(authWithName("carol@example.com"));

		assertThat(provider.validateToken(expired)).isFalse();
	}

	@Test
	void validateToken_returnsFalse_forWrongSignature() {
		// Génère un token avec une AUTRE clé
		JwtTokenProvider other = new JwtTokenProvider();
		ReflectionTestUtils.setField(other, "jwtSecret",
			"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"); // 64 chars
		ReflectionTestUtils.setField(other, "jwtExpirationMs", 3_600_000L);

		String forged = other.generateToken(authWithName("dave@example.com"));

		// Validation avec la clé du provider courant -> doit échouer
		assertThat(provider.validateToken(forged)).isFalse();
	}

	@Test
	void getUsernameFromJwt_throws_forInvalidToken() {
		assertThrows(JwtException.class, () -> provider.getUsernameFromJwt("invalid.jwt.token"));
	}
}
