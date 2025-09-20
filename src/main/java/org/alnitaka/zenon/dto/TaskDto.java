package org.alnitaka.zenon.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import org.alnitaka.zenon.entity.Task;

/**
 * DTO for {@link Task}
 */
public record TaskDto(
	Long id,
	@NotNull String nom,
	boolean active
) implements Serializable {
}
