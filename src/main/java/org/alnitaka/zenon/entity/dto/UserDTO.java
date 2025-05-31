package org.alnitaka.zenon.entity.dto;

import java.util.Date;
import java.util.Set;
import org.alnitaka.zenon.security.Role;

public record UserDTO(
	Long id,
	Date dateCreation,
	String email,
	String lastname,
	String firstname,
	boolean active,
	Set<Role> roles
) {
}
