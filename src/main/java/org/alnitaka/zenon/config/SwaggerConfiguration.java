package org.alnitaka.zenon.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

	@Bean
	public GroupedOpenApi allEndpoint() {
		String[] paths = {"/api/**",};
		return GroupedOpenApi.builder().group("All").pathsToMatch(paths).build();
	}

	@Bean
	public GroupedOpenApi Authentication() {
		String[] paths = {"/api/auth/**",};
		return GroupedOpenApi.builder().group("Authentication").pathsToMatch(paths).build();
	}

	@Bean
	public GroupedOpenApi User() {
		String[] paths = {"/api/users/**",};
		return GroupedOpenApi.builder().group("User").pathsToMatch(paths).build();
	}

	@Bean
	public GroupedOpenApi Client() {
		String[] paths = {"/api/clients/**",};
		return GroupedOpenApi.builder().group("Client").pathsToMatch(paths).build();
	}

	@Bean
	public GroupedOpenApi Project() {
		String[] paths = {"/api/project/**",};
		return GroupedOpenApi.builder().group("Project").pathsToMatch(paths).build();
	}
}
