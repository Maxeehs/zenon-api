package org.alnitaka.zenon.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.alnitaka.zenon.dto.ClientDto;
import org.alnitaka.zenon.dto.ProjectDto;
import org.alnitaka.zenon.dto.UserDto;
import org.alnitaka.zenon.entity.Client;
import org.alnitaka.zenon.entity.Project;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.mapper.ProjectMapper;
import org.alnitaka.zenon.repository.ClientRepository;
import org.alnitaka.zenon.repository.ProjectRepository;
import org.alnitaka.zenon.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

	@Mock
	ProjectRepository projectRepo;
	@Mock
	UserRepository userRepository;
	@Mock
	ClientRepository clientRepository;
	@Mock
	ProjectMapper projectMapper;
	@Mock
	UserService userService;

	@InjectMocks
	ProjectService service;

	// ---------- listProjects
	@Test
	void listProjects_returnsProjects_forAuthenticatedUser() {
		User me = new User();
		me.setId(1L);
		me.setEmail("me@mail.test");
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		List<Project> expected = List.of(new Project(), new Project());
		when(projectRepo.findByOwnerEmail("me@mail.test")).thenReturn(expected);

		List<Project> result = service.listProjects();

		assertThat(result).isSameAs(expected);
		verify(projectRepo).findByOwnerEmail("me@mail.test");
		verifyNoMoreInteractions(projectRepo);
	}

	@Test
	void listProjects_throwsAccessDenied_whenNoUser() {
		when(userService.getCurrentUser()).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.listProjects()).isInstanceOf(AccessDeniedException.class);

		verifyNoInteractions(projectRepo);
	}

	// ---------- getProject

	@Test
	void getProject_returnsProject_whenFoundForOwner() {
		User me = new User();
		me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Project p = new Project();
		p.setId(10L);
		when(projectRepo.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(p));

		Project result = service.getProject(10L);

		assertThat(result).isSameAs(p);
		verify(projectRepo).findByIdAndOwnerId(10L, 1L);
		verifyNoMoreInteractions(projectRepo);
	}

	@Test
	void getProject_throwsAccessDenied_whenNoUser() {
		when(userService.getCurrentUser()).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getProject(10L)).isInstanceOf(AccessDeniedException.class);

		verifyNoInteractions(projectRepo);
	}

	@Test
	void getProject_throwsEntityNotFound_whenMissingForOwner() {
		User me = new User();
		me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));
		when(projectRepo.findByIdAndOwnerId(123L, 1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getProject(123L)).isInstanceOf(EntityNotFoundException.class);

		verify(projectRepo).findByIdAndOwnerId(123L, 1L);
		verifyNoMoreInteractions(projectRepo);
	}

	// ---------- create

	@Test
	void create_succeeds_withoutClient_setsOwner_andSaves() {
		// Arrange
		ProjectDto dto = mock(ProjectDto.class);
		Project mapped = new Project();
		when(projectMapper.toEntity(dto)).thenReturn(mapped);

		User me = new User();
		me.setId(7L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		when(dto.client()).thenReturn(null); // pas de client
		when(projectRepo.save(mapped)).thenAnswer(inv -> inv.getArgument(0));

		// Act
		Project result = service.create(dto);

		// Assert
		assertThat(result).isSameAs(mapped);
		assertThat(result.getOwner()).isSameAs(me);
		assertThat(result.getClient()).isNull();

		verify(projectMapper).toEntity(dto);
		verify(projectRepo).save(mapped);
		verifyNoMoreInteractions(projectMapper, projectRepo);
		verifyNoInteractions(clientRepository, userRepository);
	}

	@Test
	void create_succeeds_withClient_setsOwnerAndClient_andSaves() {
		ProjectDto dto = mock(ProjectDto.class);
		Project mapped = new Project();
		when(projectMapper.toEntity(dto)).thenReturn(mapped);

		User me = new User();
		me.setId(7L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		ClientDto clientDto = mock(ClientDto.class);
		when(dto.client()).thenReturn(clientDto);
		when(clientDto.id()).thenReturn(55L);

		Client c = new Client();
		c.setId(55L);
		when(clientRepository.findById(55L)).thenReturn(Optional.of(c));

		when(projectRepo.save(mapped)).thenAnswer(inv -> inv.getArgument(0));

		Project result = service.create(dto);

		assertThat(result).isSameAs(mapped);
		assertThat(result.getOwner()).isSameAs(me);
		assertThat(result.getClient()).isSameAs(c);

		verify(clientRepository).findById(55L);
		verify(projectMapper).toEntity(dto);
		verify(projectRepo).save(mapped);
		verifyNoMoreInteractions(clientRepository, projectMapper, projectRepo);
		verifyNoInteractions(userRepository);
	}

	@Test
	void create_throwsAccessDenied_whenNoUser() {
		// Dans ton implémentation, le mapper est appelé AVANT la vérif d’auth :
		// on stubbe donc toEntity pour éviter un NullPointer et un UnnecessaryStubbing.
		when(userService.getCurrentUser()).thenReturn(Optional.empty());
		ProjectDto dto = mock(ProjectDto.class);

		assertThatThrownBy(() -> service.create(dto)).isInstanceOf(AccessDeniedException.class);

		// Pas de save ni de lookup client attendu
		verifyNoInteractions(projectMapper, projectRepo, clientRepository, userRepository);
	}

	@Test
	void create_withClientDtoButNullId_setsOwner_doesNotLookupClient_andSaves() {
		// Arrange
		User me = new User();
		me.setId(7L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		// dto.client() existe mais son id est null
		ClientDto clientDto = mock(ClientDto.class);
		when(clientDto.id()).thenReturn(null);

		ProjectDto dto = mock(ProjectDto.class);
		when(dto.client()).thenReturn(clientDto);

		Project mapped = new Project();
		when(projectMapper.toEntity(dto)).thenReturn(mapped);

		when(projectRepo.save(mapped)).thenAnswer(inv -> inv.getArgument(0));

		// Act
		Project result = service.create(dto);

		// Assert
		assertThat(result).isSameAs(mapped);
		assertThat(result.getOwner()).isSameAs(me);
		assertThat(result.getClient()).isNull(); // pas de client assigné

		verify(projectMapper).toEntity(dto);
		verify(projectRepo).save(mapped);
		verifyNoInteractions(clientRepository);   // ❗ aucun lookup client
		verifyNoInteractions(userRepository);
	}

	// ---------- update

	@Test
	void update_succeeds_withoutOwnerChange_butUpdatesName_andSetsClientWhenProvided() {
		User me = new User();
		me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Project existing = new Project();
		existing.setId(10L);
		User currentOwner = new User();
		currentOwner.setId(99L);
		existing.setOwner(currentOwner);

		when(projectRepo.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(existing));

		// dto.owner.id == existing.owner.id => pas de changement d'owner
		UserDto ownerDto = mock(UserDto.class);
		when(ownerDto.id()).thenReturn(99L);

		// client différent => doit setClient(newClient)
		ClientDto clientDto = mock(ClientDto.class);
		when(clientDto.id()).thenReturn(55L);

		ProjectDto dto = mock(ProjectDto.class);
		when(dto.id()).thenReturn(10L);
		when(dto.nom()).thenReturn("New Name");
		when(dto.owner()).thenReturn(ownerDto);
		when(dto.client()).thenReturn(clientDto);

		Client newClient = new Client();
		newClient.setId(55L);
		when(clientRepository.findById(55L)).thenReturn(Optional.of(newClient));

		when(projectRepo.save(existing)).thenAnswer(inv -> inv.getArgument(0));

		Project result = service.update((ProjectDto) dto);

		assertThat(result).isSameAs(existing);
		assertThat(result.getNom()).isEqualTo("New Name");
		assertThat(result.getOwner()).isSameAs(currentOwner);
		assertThat(result.getClient()).isSameAs(newClient);

		verify(clientRepository).findById(55L);
		verify(projectRepo).findByIdAndOwnerId(10L, 1L);
		verify(projectRepo).save(existing);
		verifyNoMoreInteractions(clientRepository, projectRepo);
		verifyNoInteractions(userRepository);
	}

	@Test
	void update_succeeds_withOwnerChange() {
		User me = new User();
		me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Project existing = new Project();
		existing.setId(10L);
		User oldOwner = new User();
		oldOwner.setId(99L);
		existing.setOwner(oldOwner);

		when(projectRepo.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(existing));

		UserDto ownerDto = mock(UserDto.class);
		when(ownerDto.id()).thenReturn(77L);

		ProjectDto dto = mock(ProjectDto.class);
		when(dto.id()).thenReturn(10L);
		when(dto.nom()).thenReturn("N");
		when(dto.owner()).thenReturn(ownerDto);
		when(dto.client()).thenReturn(null); // pas de client

		User newOwner = new User();
		newOwner.setId(77L);
		when(userRepository.findById(77L)).thenReturn(Optional.of(newOwner));

		when(projectRepo.save(existing)).thenAnswer(inv -> inv.getArgument(0));

		Project result = service.update(dto);

		assertThat(result.getOwner()).isSameAs(newOwner);
		assertThat(result.getNom()).isEqualTo("N");
		assertThat(result.getClient()).isNull();

		verify(userRepository).findById(77L);
		verify(projectRepo).findByIdAndOwnerId(10L, 1L);
		verify(projectRepo).save(existing);
		verifyNoMoreInteractions(userRepository, projectRepo);
		verifyNoInteractions(clientRepository);
	}

	@Test
	void update_setsClientNull_whenDtoClientIsNull() {
		User me = new User();
		me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Project existing = new Project();
		existing.setId(10L);
		existing.setClient(new Client()); // il y avait un client avant

		when(projectRepo.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(existing));

		ProjectDto dto = mock(ProjectDto.class);
		when(dto.id()).thenReturn(10L);
		when(dto.nom()).thenReturn("X");
		when(dto.client()).thenReturn(null);
		when(dto.owner()).thenReturn(null);

		when(projectRepo.save(existing)).thenAnswer(inv -> inv.getArgument(0));

		Project result = service.update(dto);

		assertThat(result.getClient()).isNull();
		assertThat(result.getNom()).isEqualTo("X");

		verify(projectRepo).findByIdAndOwnerId(10L, 1L);
		verify(projectRepo).save(existing);
		verifyNoMoreInteractions(projectRepo);
		verifyNoInteractions(clientRepository, userRepository);
	}

	@Test
	void update_throwsAccessDenied_whenNoUser() {
		when(userService.getCurrentUser()).thenReturn(Optional.empty());
		ProjectDto dto = mock(ProjectDto.class);

		assertThatThrownBy(() -> service.update(dto)).isInstanceOf(AccessDeniedException.class);

		verifyNoInteractions(projectRepo, userRepository, clientRepository);
	}

	@Test
	void update_throwsEntityNotFound_whenMissingForOwner() {
		User me = new User();
		me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		ProjectDto dto = mock(ProjectDto.class);
		when(dto.id()).thenReturn(123L);

		when(projectRepo.findByIdAndOwnerId(123L, 1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.update(dto)).isInstanceOf(EntityNotFoundException.class);

		verify(projectRepo).findByIdAndOwnerId(123L, 1L);
		verifyNoMoreInteractions(projectRepo);
		verifyNoInteractions(userRepository, clientRepository);
	}

	@Test
	void update_doesNotChangeOwner_whenExistingOwnerIsNull_evenIfDtoOwnerProvided() {
		// Arrange
		User me = new User();
		me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Project existing = new Project();
		existing.setId(10L);
		existing.setOwner(null); // owner inexistant

		when(projectRepo.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(existing));

		UserDto ownerDto = mock(UserDto.class);
		lenient().when(ownerDto.id()).thenReturn(77L); // fourni, mais ne doit pas être utilisé

		ProjectDto dto = mock(ProjectDto.class);
		when(dto.id()).thenReturn(10L);
		when(dto.nom()).thenReturn("New Name");
		when(dto.owner()).thenReturn(ownerDto);
		when(dto.client()).thenReturn(null); // pas de client

		when(projectRepo.save(existing)).thenAnswer(inv -> inv.getArgument(0));

		// Act
		Project result = service.update(dto);

		// Assert
		assertThat(result).isSameAs(existing);
		assertThat(result.getNom()).isEqualTo("New Name");
		assertThat(result.getOwner()).isNull(); // inchangé
		assertThat(result.getClient()).isNull();

		verify(projectRepo).findByIdAndOwnerId(10L, 1L);
		verify(projectRepo).save(existing);
		verifyNoMoreInteractions(projectRepo);
		verifyNoInteractions(userRepository); // ❗ pas de lookup owner
		verifyNoInteractions(clientRepository);
	}

	// ---------- delete

	@Test
	void delete_succeeds_whenFoundForOwner() {
		User me = new User();
		me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Project existing = new Project();
		existing.setId(10L);
		when(projectRepo.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(existing));

		service.delete(10L);

		verify(projectRepo).findByIdAndOwnerId(10L, 1L);
		verify(projectRepo).delete(existing);
		verifyNoMoreInteractions(projectRepo);
	}

	@Test
	void delete_throwsAccessDenied_whenNoUser() {
		when(userService.getCurrentUser()).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete(10L)).isInstanceOf(AccessDeniedException.class);

		verifyNoInteractions(projectRepo);
	}

	@Test
	void delete_throwsEntityNotFound_whenMissingForOwner() {
		User me = new User();
		me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));
		when(projectRepo.findByIdAndOwnerId(999L, 1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete(999L)).isInstanceOf(EntityNotFoundException.class);

		verify(projectRepo).findByIdAndOwnerId(999L, 1L);
		verifyNoMoreInteractions(projectRepo);
	}
}
