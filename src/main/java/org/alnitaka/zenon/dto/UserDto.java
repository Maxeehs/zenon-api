package org.alnitaka.zenon.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.alnitaka.zenon.security.Role;

/**
 * DTO for {@link org.alnitaka.zenon.entity.User}
 */
public record UserDto(Long id, Date dateCreation, @NotNull String email, String lastname, String firstname,
					  boolean active, Set<Role> roles) implements Serializable {
}
