package org.alnitaka.zenon.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.alnitaka.zenon.dto.ClientDto;
import org.alnitaka.zenon.dto.UserDto;
import org.alnitaka.zenon.entity.Client;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.mapper.ClientMapper;
import org.alnitaka.zenon.repository.ClientRepository;
import org.alnitaka.zenon.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

	@InjectMocks
	private ClientService clientService;
	@Mock
	private ClientRepository clientRepo;
	@Mock
	private UserRepository userRepository;
	@Mock
	private UserService userService;
	@Mock
	private ClientMapper clientMapper;

	// listMyClients()
	@Test
	void testListMyClients_NoAuth() {
		when(userService.getCurrentUser()).thenReturn(Optional.empty());
		assertThrows(AccessDeniedException.class, () -> clientService.listMyClients());
	}

	@Test
	void testListMyClients_EmptyClientList() {
		User user = new User();
		user.setEmail("test@example.com");
		when(userService.getCurrentUser()).thenReturn(Optional.of(user));
		when(clientRepo.findByOwnerEmail("test@example.com")).thenReturn(List.of());

		List<Client> clients = clientService.listMyClients();
		assertTrue(clients.isEmpty());
	}

	@Test
	void testListMyClients_WithClients() {
		User user = new User();
		user.setEmail("test@example.com");
		when(userService.getCurrentUser()).thenReturn(Optional.of(user));

		Client client1 = new Client();
		client1.setNom("Client 1");
		client1.setEmail("client1@example.com");
		client1.setOwner(user);

		Client client2 = new Client();
		client2.setNom("Client 2");
		client2.setEmail("client2@example.com");
		client2.setOwner(user);

		when(clientRepo.findByOwnerEmail("test@example.com")).thenReturn(Arrays.asList(client1, client2));

		List<Client> clients = clientService.listMyClients();
		assertEquals(2, clients.size());
		assertTrue(clients.contains(client1));
		assertTrue(clients.contains(client2));
	}

	// getClient()
	@Test
	void getClient_returnsClient_whenUserAuthenticatedAndFound() {
		// Arrange
		User me = new User();
		me.setId(42L);

		Client client = new Client();
		client.setId(10L);

		when(userService.getCurrentUser()).thenReturn(Optional.of(me));
		when(clientRepo.findByIdAndOwnerId(10L, 42L)).thenReturn(Optional.of(client));

		// Act
		Client result = clientService.getClient(10L);

		// Assert
		assertThat(result).isSameAs(client);

		// Vérifie les bons arguments transmis au repo
		ArgumentCaptor<Long> idCap = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Long> ownerCap = ArgumentCaptor.forClass(Long.class);
		verify(clientRepo).findByIdAndOwnerId(idCap.capture(), ownerCap.capture());
		assertThat(idCap.getValue()).isEqualTo(10L);
		assertThat(ownerCap.getValue()).isEqualTo(42L);

		verifyNoMoreInteractions(clientRepo);
	}

	@Test
	void getClient_throwsAccessDenied_whenNoCurrentUser() {
		// Arrange
		when(userService.getCurrentUser()).thenReturn(Optional.empty());

		// Act + Assert
		assertThrows(AccessDeniedException.class, () -> clientService.getClient(123L));

		// Le repo ne doit jamais être appelé
		verifyNoInteractions(clientRepo);
	}

	@Test
	void getClient_throwsEntityNotFound_whenClientMissingForOwner() {
		// Arrange
		User me = new User();
		me.setId(42L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));
		when(clientRepo.findByIdAndOwnerId(123L, 42L)).thenReturn(Optional.empty());

		// Act + Assert
		assertThrows(EntityNotFoundException.class, () -> clientService.getClient(123L));
		verify(clientRepo).findByIdAndOwnerId(123L, 42L);
		verifyNoMoreInteractions(clientRepo);
	}

	// create()
	@Test
	void create_succeeds_setsOwner_andSaves() {
		// Arrange
		UserDto userDto = new UserDto(114L, new Date(), "test@123.fr", "lastname", "firstname", true, new HashSet<>());
		ClientDto clientDto = new ClientDto(10L, "nom", "email@test.fr", userDto);

		Client mapped = new Client();   // entité issue du mapper
		when(clientMapper.toEntity(clientDto)).thenReturn(mapped);

		User me = new User();
		me.setId(42L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Client saved = new Client();
		saved.setId(100L);
		when(clientRepo.save(mapped)).thenReturn(saved);

		// Act
		Client result = clientService.create(clientDto);

		// Assert
		assertThat(result).isSameAs(saved);

		// le mapper a bien été appelé
		verify(clientMapper).toEntity(clientDto);

		// l’owner a bien été positionné AVANT l’appel au save
		ArgumentCaptor<Client> toSave = ArgumentCaptor.forClass(Client.class);
		verify(clientRepo).save(toSave.capture());
		Client captured = toSave.getValue();
		assertThat(captured).isSameAs(mapped);
		assertThat(captured.getOwner()).isSameAs(me);

		verifyNoMoreInteractions(clientRepo, clientMapper);
	}

	@Test
	void create_throwsAccessDenied_whenNoCurrentUser() {
		// Arrange
		UserDto userDto = new UserDto(114L, new Date(), "test@123.fr", "lastname", "firstname", true, new HashSet<>());
		ClientDto clientDto = new ClientDto(10L, "nom", "email@test.fr", userDto);
		when(userService.getCurrentUser()).thenReturn(Optional.empty());

		// Act + Assert
		assertThrows(AccessDeniedException.class, () -> clientService.create(clientDto));

		// Pas de mapping ni de save si pas d’utilisateur
		verifyNoInteractions(clientRepo);
		verifyNoInteractions(clientMapper);
	}

	// update()
	@Test
	void update_succeeds_withoutOwnerChange() {
		// Arrange
		User me = new User(); me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Client existing = new Client();
		existing.setId(10L);
		User currentOwner = new User(); currentOwner.setId(99L);
		existing.setOwner(currentOwner);
		existing.setNom("Old");
		existing.setEmail("old@mail.test");

		when(clientRepo.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(existing));

		ClientDto dto = mock(ClientDto.class);
		when(dto.id()).thenReturn(10L);
		when(dto.nom()).thenReturn("New Name");
		when(dto.email()).thenReturn("new@mail.test");
		when(dto.owner()).thenReturn(null); // pas de changement d’owner

		// on renvoie "existing" tel quel lors du save
		when(clientRepo.save(existing)).thenAnswer(inv -> inv.getArgument(0));

		// Act
		Client result = clientService.update(dto);

		// Assert
		assertThat(result).isSameAs(existing);
		assertThat(result.getNom()).isEqualTo("New Name");
		assertThat(result.getEmail()).isEqualTo("new@mail.test");
		assertThat(result.getOwner()).isSameAs(currentOwner); // owner intact

		// le repo user n’est pas sollicité si pas de changement d’owner
		verifyNoInteractions(userRepository);
		verify(clientRepo).findByIdAndOwnerId(10L, 1L);
		verify(clientRepo).save(existing);
		verifyNoMoreInteractions(clientRepo);
	}

	@Test
	void update_succeeds_withOwnerChange() {
		// Arrange
		User me = new User(); me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Client existing = new Client();
		existing.setId(10L);
		User oldOwner = new User(); oldOwner.setId(99L);
		existing.setOwner(oldOwner);

		when(clientRepo.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(existing));

		UserDto ownerDto = mock(UserDto.class);
		when(ownerDto.id()).thenReturn(77L);

		ClientDto dto = mock(ClientDto.class);
		when(dto.id()).thenReturn(10L);
		when(dto.nom()).thenReturn("N");
		when(dto.email()).thenReturn("n@test");
		when(dto.owner()).thenReturn(ownerDto);

		User newOwner = new User(); newOwner.setId(77L);
		when(userRepository.findById(77L)).thenReturn(Optional.of(newOwner));

		when(clientRepo.save(existing)).thenAnswer(inv -> inv.getArgument(0));

		// Act
		Client result = clientService.update(dto);

		// Assert
		assertThat(result.getOwner()).isSameAs(newOwner);
		assertThat(result.getNom()).isEqualTo("N");
		assertThat(result.getEmail()).isEqualTo("n@test");

		verify(userRepository).findById(77L);
		verify(clientRepo).findByIdAndOwnerId(10L, 1L);
		verify(clientRepo).save(existing);
		verifyNoMoreInteractions(userRepository, clientRepo);
	}

	@Test
	void update_throwsAccessDenied_whenNoCurrentUser() {
		// Arrange
		when(userService.getCurrentUser()).thenReturn(Optional.empty());
		ClientDto dto = mock(ClientDto.class);

		// Act + Assert
		assertThrows(AccessDeniedException.class, () -> clientService.update(dto));

		verifyNoInteractions(clientRepo, userRepository);
	}

	@Test
	void update_throwsEntityNotFound_whenClientMissingForOwner() {
		// Arrange
		User me = new User(); me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		ClientDto dto = mock(ClientDto.class);
		when(dto.id()).thenReturn(123L);

		when(clientRepo.findByIdAndOwnerId(123L, 1L)).thenReturn(Optional.empty());

		// Act + Assert
		assertThrows(EntityNotFoundException.class, () -> clientService.update(dto));

		verify(clientRepo).findByIdAndOwnerId(123L, 1L);
		verifyNoMoreInteractions(clientRepo);
		verifyNoInteractions(userRepository);
	}

	@Test
	void update_ownerIsNull_onExisting_shouldNotChangeOwner_evenIfDtoOwnerProvided() {
		// Arrange
		User me = new User(); me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Client existing = new Client();
		existing.setId(10L);
		existing.setOwner(null); // owner inexistant

		when(clientRepo.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(existing));

		UserDto ownerDto = mock(UserDto.class);
		lenient().when(ownerDto.id()).thenReturn(77L);

		ClientDto dto = mock(ClientDto.class);
		when(dto.id()).thenReturn(10L);
		when(dto.nom()).thenReturn("X");
		when(dto.email()).thenReturn("x@test");
		lenient().when(dto.owner()).thenReturn(ownerDto);

		when(clientRepo.save(existing)).thenAnswer(inv -> inv.getArgument(0));

		// Act
		Client result = clientService.update(dto);

		// Assert
		// Comme la condition exige owner != null, rien ne change côté owner
		assertThat(result.getOwner()).isNull();

		// userRepository ne doit PAS être appelé
		verifyNoInteractions(userRepository);
		verify(clientRepo).findByIdAndOwnerId(10L, 1L);
		verify(clientRepo).save(existing);
		verifyNoMoreInteractions(clientRepo);
	}

	@Test
	void update_keepsOwner_whenDtoOwnerIdEqualsExistingOwnerId() {
		// Arrange
		User me = new User(); me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Client existing = new Client();
		existing.setId(10L);
		User currentOwner = new User(); currentOwner.setId(42L);
		existing.setOwner(currentOwner);

		when(clientRepo.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(existing));

		// dto.owner().id() == existing.owner.id -> pas de changement d'owner attendu
		org.alnitaka.zenon.dto.UserDto ownerDto = mock(org.alnitaka.zenon.dto.UserDto.class);
		when(ownerDto.id()).thenReturn(42L);

		org.alnitaka.zenon.dto.ClientDto dto = mock(org.alnitaka.zenon.dto.ClientDto.class);
		when(dto.id()).thenReturn(10L);
		when(dto.nom()).thenReturn("New Name");
		when(dto.email()).thenReturn("new@mail.test");
		when(dto.owner()).thenReturn(ownerDto);

		when(clientRepo.save(existing)).thenAnswer(inv -> inv.getArgument(0));

		// Act
		Client result = clientService.update(dto);

		// Assert
		// 1) Les champs simples sont bien mis à jour
		assertThat(result.getNom()).isEqualTo("New Name");
		assertThat(result.getEmail()).isEqualTo("new@mail.test");

		// 2) L’owner ne bouge pas
		assertThat(result.getOwner()).isSameAs(currentOwner);

		// 3) On NE va pas chercher un nouvel owner en base
		verify(userRepository, never()).findById(anyLong());

		// 4) Le repo client est bien utilisé
		verify(clientRepo).findByIdAndOwnerId(10L, 1L);
		verify(clientRepo).save(existing);
		verifyNoMoreInteractions(clientRepo, userRepository);
	}

	// delete()
	@Test
	void delete_succeeds_whenUserAuthenticatedAndClientFound() {
		// Arrange
		long id = 10L;
		User me = new User(); me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));

		Client existing = new Client(); existing.setId(id);
		when(clientRepo.findByIdAndOwnerId(id, 1L)).thenReturn(Optional.of(existing));

		// Act
		clientService.delete(id);

		// Assert
		verify(clientRepo).findByIdAndOwnerId(id, 1L);
		verify(clientRepo).delete(existing);
		verifyNoMoreInteractions(clientRepo);
	}

	@Test
	void delete_throwsAccessDenied_whenNoCurrentUser() {
		// Arrange
		when(userService.getCurrentUser()).thenReturn(Optional.empty());

		// Act + Assert
		assertThatThrownBy(() -> clientService.delete(123L))
			.isInstanceOf(AccessDeniedException.class);

		verifyNoInteractions(clientRepo);
	}

	@Test
	void delete_throwsEntityNotFound_whenClientMissingForOwner() {
		// Arrange
		long id = 999L;
		User me = new User(); me.setId(1L);
		when(userService.getCurrentUser()).thenReturn(Optional.of(me));
		when(clientRepo.findByIdAndOwnerId(id, 1L)).thenReturn(Optional.empty());

		// Act + Assert
		assertThatThrownBy(() -> clientService.delete(id))
			.isInstanceOf(EntityNotFoundException.class);

		verify(clientRepo).findByIdAndOwnerId(id, 1L);
		verifyNoMoreInteractions(clientRepo);
	}
}
