package org.alnitaka.zenon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.alnitaka.zenon.dto.UserDto;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.mapper.UserMapper;
import org.alnitaka.zenon.repository.UserRepository;
import org.alnitaka.zenon.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "User")
@RequiredArgsConstructor
@RequestMapping(value = "/api/users")
public class UserController {

	private final UserRepository userRepository;
	private final UserService userService;
	private final UserMapper userMapper;

	@GetMapping("/me")
	@Operation(summary = "Récupère l'utilisateur courant")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Operation successful",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
		),
		@ApiResponse(
			responseCode = "404",
			description = "Resource not found",
			content = @Content
		)
	})
	public ResponseEntity<UserDto> getCurrentUser() {
		return userService.getCurrentUser()
			.map((User user) -> ResponseEntity.ok(userMapper.toDto(user)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Récupère un utilisateur par son ID")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Operation successful",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
		),
		@ApiResponse(
			responseCode = "404",
			description = "Resource not found",
			content = @Content
		)
	})
	public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
		return userRepository.findById(id)
			.map((User user) -> ResponseEntity.ok().body(userMapper.toDto(user)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/mail/{email}")
	@Operation(summary = "Récupère un utilisateur par son email")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Operation successful",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDto.class))
		),
		@ApiResponse(
			responseCode = "404",
			description = "Resource not found",
			content = @Content
		)
	})
	public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
		return userRepository.findUserByEmail(email)
			.map((User user) -> ResponseEntity.ok().body(userMapper.toDto(user)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
