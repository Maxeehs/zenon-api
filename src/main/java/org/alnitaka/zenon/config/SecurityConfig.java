package org.alnitaka.zenon.config;

import java.util.List;
import java.util.stream.Collectors;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.repository.UserRepository;
import org.alnitaka.zenon.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
						.anyRequest().authenticated()
				).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}

	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		return email -> userRepository.findUserByEmail(email)
				.map((User u) -> {
					List<SimpleGrantedAuthority> authorities = u.getRoles().stream()
							.map(role -> new SimpleGrantedAuthority(role.name()))
							.collect(Collectors.toList());
					return org.springframework.security.core.userdetails.User
							.withUsername(u.getEmail())
							.password(u.getPassword())
							.authorities(authorities)
							.accountExpired(false)
							.accountLocked(false)
							.credentialsExpired(false)
							.disabled(!u.isActive())
							.build();
				})
				.orElseThrow(() ->
						new UsernameNotFoundException("Utilisateur non trouvé pour email : " + email)
				);
	}
}
