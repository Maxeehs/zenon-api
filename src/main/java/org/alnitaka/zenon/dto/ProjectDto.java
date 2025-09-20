package org.alnitaka.zenon.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import org.alnitaka.zenon.entity.Project;

/**
 * DTO for {@link Project}
 */
public record ProjectDto(
	Long id,
	@NotNull String nom,
	UserDto owner,
	ClientDto client,
	List<TaskDto> tasks
) implements Serializable {
}
