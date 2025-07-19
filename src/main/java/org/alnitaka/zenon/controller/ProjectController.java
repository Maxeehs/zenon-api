package org.alnitaka.zenon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.alnitaka.zenon.entity.Project;
import org.alnitaka.zenon.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Project")
@RequiredArgsConstructor
@RequestMapping("/api/project")
public class ProjectController {
	private final ProjectService projectService;

	@GetMapping
	@Operation(summary = "Liste les projets de l'utilisateur courant")
	public List<Project> getMyProjects() {
		return projectService.listProjects();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Récupère un projet par son ID")
	public Project getMyProjects(@PathVariable Long id) {
		return projectService.getProject(id);
	}

	@PostMapping
	@Operation(summary = "Crée un nouveau projet")
	public ResponseEntity<Project> create(@RequestBody @Valid Project client) {
		Project saved = projectService.create(client);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Met à jour un projet existant")
	public Project update(@PathVariable Long id,
						 @RequestBody @Valid Project client) {
		return projectService.update(id, client);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Supprime un projet par son ID")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		projectService.delete(id);
	}
}
