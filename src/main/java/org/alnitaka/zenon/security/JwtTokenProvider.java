package org.alnitaka.zenon.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration-ms}")
	private long jwtExpirationMs;

	// 1. Génération du token
	public String generateToken(Authentication authentication) {
		String username = authentication.getName();
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtExpirationMs);

		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(now)
				.setExpiration(expiry)
				.signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS512)
				.compact();
	}

	// 2. Lecture du username
	public String getUsernameFromJwt(String token) {
		return Jwts.parser()
				.setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	// 3. Validation du token
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
					.setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
					.build()
					.parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			 log.warn("JWT invalide", e);
			return false;
		}
	}
}
