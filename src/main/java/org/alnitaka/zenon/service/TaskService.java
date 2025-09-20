package org.alnitaka.zenon.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.alnitaka.zenon.dto.TaskDto;
import org.alnitaka.zenon.entity.Project;
import org.alnitaka.zenon.entity.Task;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.mapper.TaskMapper;
import org.alnitaka.zenon.repository.ProjectRepository;
import org.alnitaka.zenon.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {
	private final TaskRepository taskRepository;
	private final ProjectRepository projectRepository;
	private final TaskMapper taskMapper;
	private final UserService userService;
	private static final String NO_TASK = "Tâche introuvable";
	private static final String NO_AUTH = "Non authentifié";
	private static final String NO_PROJECT = "Projet introuvable";

	/**
	 * Retrieves a {@link Task} by its identifier.
	 *
	 * @param id the unique identifier of the task to retrieve
	 * @return the {@link Task} with the specified id
	 * @throws EntityNotFoundException if no task with the given id exists
	 */
	public Task getTask(Long id) {
		return taskRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException(NO_TASK));
	}

	/**
	 * Creates a new task in the specified project.
	 *
	 * @param dto the data transfer object containing the task details
	 * @param projectId the identifier of the project to which the task will be added
	 * @return the list of all tasks belonging to the project after the new task has been added
	 * @throws AccessDeniedException if the current user is not authenticated
	 * @throws EntityNotFoundException if the project with the given id does not exist or does not belong to the current user
	 */
	public List<Task> create(TaskDto dto, Long projectId) {
		User me = userService.getCurrentUser()
			.orElseThrow(() -> new AccessDeniedException(NO_AUTH));
		Project project = projectRepository.findByIdAndOwnerId(projectId, me.getId())
			.orElseThrow(() -> new EntityNotFoundException(NO_PROJECT));
		Task task = taskMapper.toEntity(dto);
		task.setProject(project);
		project.getTasks().add(task);
		project = projectRepository.save(project);
		return project.getTasks();
	}

	/**
	 * Updates an existing {@link Task} with the information supplied in the {@link TaskDto}.
	 *
	 * @param dto the data transfer object containing the updated task information
	 * @return the updated {@link Task} entity
	 */
	public Task update(TaskDto dto) {
		Task existing = taskRepository.findById(dto.id())
			.orElseThrow(() -> new EntityNotFoundException(NO_TASK));
		existing.setNom(dto.nom());
		existing.setActive(dto.active());
		return taskRepository.save(existing);
	}

	/**
	 * Deletes the {@link Task} identified by the given {@code id}.
	 *
	 * @param id the unique identifier of the {@link Task} to delete
	 */
	public void delete(Long id) {
		Task existing = taskRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException(NO_TASK));
		taskRepository.delete(existing);
	}
}
