package org.alnitaka.zenon.entity.request;

import lombok.Data;

@Data
public class RegisterRequest {
	private String email;
	private String password;
	private String firstname;
	private String lastname;
}
