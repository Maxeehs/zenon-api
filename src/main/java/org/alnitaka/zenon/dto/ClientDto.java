package org.alnitaka.zenon.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO for {@link org.alnitaka.zenon.entity.Client}
 */
public record ClientDto(
	Long id,
	@NotNull String nom,
	String email,
	UserDto owner
) implements Serializable {
}
