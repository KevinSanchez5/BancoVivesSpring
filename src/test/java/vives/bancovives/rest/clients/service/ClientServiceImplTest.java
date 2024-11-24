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
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
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
    Client client ;
    ClientCreateDto createDto;
    ClientUpdateDto updateDto;
    ClientResponseDto responseDto;


    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientMapper clientMapper;
    @Mock
    private ClientUpdateValidator updateValidator;

    @InjectMocks
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        client = new Client(uuid, id, "12345678Z", "nameTest", address, "email@test.com", "654321987", null, null, true, false, LocalDateTime.now(), LocalDateTime.now());
        createDto = new ClientCreateDto("12345678Z", "nameTest", "email@test.com", "654321987",null,null, "streetTest", "123", "CITYTEST", "ESPAÑA");
        updateDto = ClientUpdateDto.builder().completeName("newNameTest").email("some@email.com").build();
        responseDto = new ClientResponseDto(id, "12345678Z", "nameTest", "email@test.com", "654321987", null, null, address, true, false, LocalDateTime.now().toString(), LocalDateTime.now().toString());
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

//    @Test
//    void deleteByIdLogically_Success() {
//        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
//        client.setDeleted(true);
//        when(clientRepository.save(client)).thenReturn(client);
//
//        Client client = clientService.deleteByIdLogically(id, Optional.empty());
//
//        assertAll(
//                () -> assertNotNull(client),
//                () -> assertEquals(id, client.getId()),
//                () -> assertTrue(client.isDeleted())
//        );
//
//        verify(clientRepository, times(1)).deleteById(id);
//    }

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