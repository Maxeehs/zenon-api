package org.alnitaka.zenon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.entity.request.JwtResponse;
import org.alnitaka.zenon.entity.request.LoginRequest;
import org.alnitaka.zenon.entity.request.RegisterRequest;
import org.alnitaka.zenon.repository.UserRepository;
import org.alnitaka.zenon.security.JwtTokenProvider;
import org.alnitaka.zenon.security.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication")
@RequestMapping(value = "/api/auth")
public class AuthController {
	
	private final AuthenticationManager authManager;
	private final JwtTokenProvider tokenProvider;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public AuthController(AuthenticationManager authManager, JwtTokenProvider tokenProvider, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.authManager = authManager;
		this.tokenProvider = tokenProvider;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@PostMapping("/login")
	@Operation
	public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest req) {
		Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
		String jwt = tokenProvider.generateToken(auth);
		return ResponseEntity.ok(new JwtResponse(jwt));
	}
	
	@PostMapping("/register")
	@Operation
	public ResponseEntity<JwtResponse> register(@RequestBody @Valid RegisterRequest req) {
		// Vérifier si l'utilisateur existe déjà
		if (userRepository.findUserByEmail(req.getEmail()).isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		// Créer et sauvegarder le nouvel utilisateur
		User newUser = new User();
		newUser.setEmail(req.getEmail());
		newUser.setPassword(passwordEncoder.encode(req.getPassword()));
		newUser.setFirstname(req.getFirstname());
		newUser.setLastname(req.getLastname());
		newUser.setActive(true);
		newUser.setRoles(Set.of(Role.ROLE_USER));
		userRepository.save(newUser);
		
		// Authentifier l'utilisateur pour générer le JWT
		Authentication auth = authManager.authenticate(
				new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
		);
		String jwt = tokenProvider.generateToken(auth);
		return ResponseEntity.ok(new JwtResponse(jwt));
	}
}
