package vives.bancovives.rest.clients.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import vives.bancovives.rest.accounts.dto.output.AccountResponseSimplified;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.service.AccountService;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.exceptions.ClientConflict;
import vives.bancovives.rest.clients.exceptions.ClientNotFound;
import vives.bancovives.rest.clients.mapper.ClientMapper;
import vives.bancovives.rest.clients.model.Address;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.clients.repository.ClientRepository;
import vives.bancovives.rest.clients.validators.ClientUpdateValidator;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.storage.service.StorageService;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    Address address = new Address("streetTest","123", "CITYTEST", "ESPAÑA");
    String id = IdGenerator.generateId();
    UUID uuid = UUID.randomUUID();
    Account account;
    Client client ;
    ClientCreateDto createDto;
    ClientUpdateDto updateDto;
    ClientResponseDto responseDto;
    UserResponse userResponse;
    AccountResponseSimplified accountResponse;


    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientMapper clientMapper;
    @Mock
    private ClientUpdateValidator updateValidator;
    @Mock
    private AccountService accountService;
    @Mock
    private UsersService userService;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        User user = new User(uuid, id, "usernameTest", "passwordTest", Collections.singleton(Role.USER), null, LocalDateTime.now(), LocalDateTime.now(), false);
        account = new Account(UUID.randomUUID(), id, "ES123456789", 0.0, "passwordTest", null, null, LocalDateTime.now(), LocalDateTime.now(), false);
        accountResponse = new AccountResponseSimplified(account.getPublicId(), account.getIban(), account.getBalance());
        client = new Client(uuid, id, "12345678Z", "nameTest", address, "email@test.com", "654321987", null, null, user, List.of(account), true, false, LocalDateTime.now(), LocalDateTime.now());
        createDto = new ClientCreateDto("12345678Z", "nameTest", "email@test.com", "654321987",null,null, "streetTest", "123", "CITYTEST", "ESPAÑA", "usernameTest", "passwordTest");
        updateDto = ClientUpdateDto.builder().completeName("newNameTest").email("some@email.com").build();
        userResponse = new UserResponse(id, "usernameTest", Collections.singleton(Role.USER), false);
        responseDto = new ClientResponseDto(id, "12345678Z", "nameTest", "email@test.com", "654321987", null, null, address, userResponse, List.of(accountResponse), true, false, LocalDateTime.now().toString(), LocalDateTime.now().toString());
        account.setClient(client);
    }


    @Test
    void findAll() {
        Page<ClientResponseDto> resultPage = new PageImpl<>(List.of(responseDto));

        when(clientRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(resultPage);

        Page<ClientResponseDto> result = clientService.findAll(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), PageRequest.of(0, 10));

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(resultPage, result),
                () -> assertEquals(1, result.getTotalElements())
        );

        verify(clientRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void findById_Success() {
        when(clientRepository.findByPublicId(id)).thenReturn(Optional.of(client));
        when(clientMapper.fromEntityToResponse(client)).thenReturn(responseDto);


        ClientResponseDto result = clientService.findById(id);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("12345678Z", result.getDni()),
                () -> assertEquals("nameTest", result.getCompleteName())
        );

        verify(clientRepository, times(1)).findByPublicId(id);
    }

    @Test
    void findById_NotFound() {
        when(clientRepository.findByPublicId(id)).thenReturn(Optional.empty());

        ClientNotFound exception = assertThrows(ClientNotFound.class, () -> clientService.findById(id));

        assertEquals("El cliente con id: " + id + " no encontrado", exception.getMessage());;

        verify(clientRepository, times(1)).findByPublicId(id);
    }

    @Test
    void save_Success() {
        when(clientMapper.fromCreateDtoToEntity(createDto)).thenReturn(client);
        when(clientRepository.findByDniIgnoreCase(createDto.getDni())).thenReturn(Optional.empty());
        when(clientRepository.findByEmailIgnoreCase(createDto.getEmail())).thenReturn(Optional.empty());
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.fromEntityToResponse(client)).thenReturn(responseDto);

        ClientResponseDto result = clientService.save(createDto);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("12345678Z", result.getDni()),
                () -> assertEquals("nameTest", result.getCompleteName())
        );

        verify(clientMapper, times(1)).fromCreateDtoToEntity(createDto);
        verify(clientRepository, times(1)).save(client);
        verify(clientRepository, times(1)).findByDniIgnoreCase(createDto.getDni());
        verify(clientRepository, times(1)).findByEmailIgnoreCase(createDto.getEmail());
    }

    @Test
    void save_DniExists() throws ClientConflict {
        when(clientRepository.findByDniIgnoreCase(createDto.getDni())).thenReturn(Optional.of(client));

        ClientConflict exception = assertThrows(ClientConflict.class, () -> clientService.save(createDto));

        assertEquals("Cliente con ese dni ya existe", exception.getMessage());

        verify(clientRepository).findByDniIgnoreCase(createDto.getDni());
        verify(clientRepository, never()).save(any());
        verify(clientRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    void save_EmailExists() throws ClientConflict {
        when(clientRepository.findByDniIgnoreCase(createDto.getDni())).thenReturn(Optional.empty());
        when(clientRepository.findByEmailIgnoreCase(createDto.getEmail())).thenReturn(Optional.of(client));

        ClientConflict exception = assertThrows(ClientConflict.class, () -> clientService.save(createDto));

        assertEquals("Ese email ya esta en uso", exception.getMessage());

        verify(clientRepository).findByDniIgnoreCase(createDto.getDni());
        verify(clientRepository).findByEmailIgnoreCase(createDto.getEmail());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void update_Success() {
        when(clientRepository.findByPublicId(id)).thenReturn(Optional.of(client));
        doNothing().when(updateValidator).validateUpdateDto(updateDto);
        when(clientMapper.fromUpdateDtoToEntity(client, updateDto)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.fromEntityToResponse(client)).thenReturn(responseDto);

        ClientResponseDto result = clientService.update(id, updateDto);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(client.getCompleteName(), result.getCompleteName()),
                () -> assertEquals("12345678Z", result.getDni()),
                () -> assertEquals("nameTest", result.getCompleteName())
        );

        verify(clientRepository, times(1)).findByPublicId(id);
        verify(updateValidator, times(1)).validateUpdateDto(updateDto);
        verify(clientMapper, times(1)).fromUpdateDtoToEntity(client, updateDto);
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void deleteByIdLogically_Success_NotDataDeleted() {
        when(clientRepository.findByPublicId(id)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(userService).deleteById(id);
        when(accountService.deleteById(id)).thenReturn(account);

        ClientResponseDto clientResponse = clientService.deleteByIdLogically(id, Optional.empty());

        assertAll(
                () -> assertNotNull(clientResponse),
                () -> assertEquals(id, clientResponse.getPublicId()),
                () -> assertTrue(clientResponse.getIsDeleted()),
                () -> assertNotNull(clientResponse.getDni()),
                () -> assertNotNull(clientResponse.getCompleteName()),
                () -> assertNotNull(clientResponse.getEmail()),
                () -> assertNotNull(clientResponse.getPhoneNumber()),
                () -> assertNull(clientResponse.getUserResponse())
        );

        verify(clientRepository, times(1)).findByPublicId(id);
        verify(userService, times(1)).deleteById(id);
        verify(accountService, times(1)).deleteById(id);
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void deleteByIdLogically_NotFound() {
        when(clientRepository.findByPublicId(id)).thenReturn(Optional.empty());

        ClientNotFound exception = assertThrows(ClientNotFound.class, () -> clientService.deleteByIdLogically(id, Optional.empty()));

        assertEquals("El cliente con id: " + id + " no encontrado", exception.getMessage());

        verify(clientRepository, times(1)).findByPublicId(id);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteDataOfClient() {
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.fromEntityToResponse(client)).thenReturn(new ClientResponseDto());

        ClientResponseDto result = clientService.deleteDataOfClient(client);

        assertAll(
                () -> assertNotNull(result),
                () -> assertInstanceOf(ClientResponseDto.class, result),
                () -> assertNull(result.getDni()),
                () -> assertNull(result.getCompleteName()),
                () -> assertNull(result.getEmail()),
                () -> assertNull(result.getPhoneNumber()),
                () -> assertNull(result.getPhoto()),
                () -> assertNull(result.getDniPicture()),
                () -> assertNull(result.getAddress())
        );
        verify(clientMapper, times(1)).fromEntityToResponse(client);
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void existsClient_ByDniAndEmail_DoesNotExist() {
        when(clientRepository.findByDniIgnoreCase(createDto.getDni())).thenReturn(Optional.empty());
        when(clientRepository.findByEmailIgnoreCase(createDto.getEmail())).thenReturn(Optional.empty());

        clientService.existsClientByDniAndEmail(createDto.getDni(), createDto.getEmail());

        verify(clientRepository).findByDniIgnoreCase(createDto.getDni());
        verify(clientRepository).findByEmailIgnoreCase(createDto.getEmail());

    }

    @Test
    void existsClient_ByDniAndEmail_ThrowConflictDni() {
        when(clientRepository.findByDniIgnoreCase(createDto.getDni())).thenReturn(Optional.of(client));

        ClientConflict exception = assertThrows(ClientConflict.class, () -> clientService.existsClientByDniAndEmail(createDto.getDni(), createDto.getEmail()));

        assertEquals("Cliente con ese dni ya existe", exception.getMessage());

        verify(clientRepository).findByDniIgnoreCase(createDto.getDni());
        verify(clientRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    void existsClient_ByDniAndEmail_ThrowConflictEmail() {
        when(clientRepository.findByDniIgnoreCase(createDto.getDni())).thenReturn(Optional.empty());
        when(clientRepository.findByEmailIgnoreCase(createDto.getEmail())).thenReturn(Optional.of(client));

        ClientConflict exception = assertThrows(ClientConflict.class, () -> clientService.existsClientByDniAndEmail(createDto.getDni(), createDto.getEmail()));

        assertEquals("Ese email ya esta en uso", exception.getMessage());

        verify(clientRepository).findByDniIgnoreCase(createDto.getDni());
        verify(clientRepository).findByEmailIgnoreCase(createDto.getEmail());
    }

    @Test
    void validateClient_Success() {
        when(clientRepository.findByPublicId(id)).thenReturn(Optional.of(client));
        client.setValidated(true);
        when(clientRepository.save(any())).thenReturn(client);
        when(clientMapper.fromEntityToResponse(client)).thenReturn(responseDto);

        ClientResponseDto result = clientService.validateClient(id);

        //TODO COMPARAR RESULTADO CON ENTIDAD CLIENTE
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.getValidated()),
                () -> assertEquals("12345678Z", result.getDni()),
                () -> assertEquals("nameTest", result.getCompleteName())
        );

        verify(clientRepository, times(1)).findByPublicId(id);
        verify(clientRepository, times(1)).save(any());
    }

    @Test
    void validateClient_NotFound() {
        when(clientRepository.findByPublicId(id)).thenReturn(Optional.empty());

        ClientNotFound exception = assertThrows(ClientNotFound.class, () -> clientService.validateClient(id));

        assertEquals("El cliente con id: " + id + " no encontrado", exception.getMessage());

        verify(clientRepository, times(1)).findByPublicId(id);
        verify(clientRepository, never()).save(any());
    }
}