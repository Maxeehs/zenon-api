package org.alnitaka.zenon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.entity.dto.UserDTO;
import org.alnitaka.zenon.repository.UserRepository;
import org.alnitaka.zenon.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "User")
@RequestMapping(value = "/api/users")
public class UserController {
	
	private final UserRepository userRepository;
	private final UserService userService;
	
	public UserController(UserRepository userRepository, UserService userService) {
		this.userRepository = userRepository;
		this.userService = userService;
	}
	
	@GetMapping
	@Operation
	public ResponseEntity<List<User>> getAllUsers() {
		return ResponseEntity.ok().body(userRepository.findAll());
	}
	
	@GetMapping("/me")
	@Operation
	public ResponseEntity<UserDTO> getCurrentUser() {
		return userService.getCurrentUser().map((User user) -> {
			UserDTO userDTO = new UserDTO(user.getId(), user.getDateCreation(), user.getEmail(), user.getLastname(), user.getFirstname(), user.isActive(), user.getRoles());
			return ResponseEntity.ok(userDTO);
		}).orElseGet(() -> ResponseEntity.status(401).build());
	}
	
	@GetMapping("/{id}")
	@Operation
	public ResponseEntity<User> getUserById(@PathVariable("id") long id) {
		userRepository.findById(id).ifPresent((User user) -> ResponseEntity.ok().body(user));
		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/{email}")
	@Operation
	public ResponseEntity<User> getUserByEmail(@PathVariable("email") String email) {
		userRepository.findUserByEmail(email).ifPresent((User user) -> ResponseEntity.ok().body(user));
		return ResponseEntity.notFound().build();
	}
}
