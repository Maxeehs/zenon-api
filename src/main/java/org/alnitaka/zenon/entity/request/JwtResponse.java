package org.alnitaka.zenon.entity.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class JwtResponse {
	private String token;
	private String type = "Bearer";
	
	public JwtResponse(String token) {
		this.token = token;
	}
}
