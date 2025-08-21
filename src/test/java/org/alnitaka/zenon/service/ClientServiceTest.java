package org.alnitaka.zenon.service;

import jakarta.security.auth.message.AuthException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.alnitaka.zenon.entity.Client;
import org.alnitaka.zenon.entity.User;
import org.alnitaka.zenon.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

	@InjectMocks
	private ClientService clientService;

	@Mock
	private ClientRepository clientRepo;

	@Mock
	private UserService userService;

	@Test
	public void testListMyClients_NoAuth() throws AuthException {
		when(userService.getCurrentUser()).thenReturn(Optional.empty());
		assertThrows(AccessDeniedException.class, () -> clientService.listMyClients());
	}

	@Test
	public void testListMyClients_EmptyClientList() throws AuthException {
		User user = new User();
		user.setEmail("test@example.com");
		when(userService.getCurrentUser()).thenReturn(Optional.of(user));
		when(clientRepo.findByOwnerEmail("test@example.com")).thenReturn(Arrays.asList());

		List<Client> clients = clientService.listMyClients();
		assertTrue(clients.isEmpty());
	}

	@Test
	public void testListMyClients_WithClients() throws AuthException {
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
}
