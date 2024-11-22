package vives.bancovives.rest.clients.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.model.Address;
import vives.bancovives.rest.clients.model.Client;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClientMapperTest {

    Address address;
    UUID id = UUID.randomUUID();
    Client client ;
    ClientCreateDto createDto;
    ClientUpdateDto updateDto;

    ClientMapper clientMapper;

    @BeforeEach
    void setUp() {
        clientMapper = new ClientMapper();
        address = new Address("streetTest","123", "CITYTEST", "ESPAÑA");
        client = new Client(id, null, "12345678Z", "nameTest", address, "email@test.com", "654321987", null, null, true, false, LocalDateTime.now(), LocalDateTime.now());
        createDto = new ClientCreateDto("12345678Z", "nameTest", "email@test.com", "654321987",null,null, "streetTest", "123", "CITYTEST", "ESPAÑA");
        updateDto = ClientUpdateDto.builder().completeName("newNameTest").email("diferent@email.com").city("Barcelona").country("aNdORra").build();
    }


    @Test
    void fromCreateDtoToEntity() {
        Client client = clientMapper.fromCreateDtoToEntity(createDto);

        assertAll(
                () -> assertInstanceOf(UUID.class, client.getId()),
                () -> assertEquals("12345678Z", client.getDni()),
                () -> assertEquals("nameTest", client.getCompleteName()),
                () -> assertEquals("email@test.com", client.getEmail()),
                () -> assertEquals("654321987", client.getPhoneNumber()),
                () -> assertEquals(address, client.getAddress()),
                () -> assertFalse(client.isValidated()),
                () -> assertFalse(client.isDeleted()),
                () -> assertNotNull(client.getCreatedAt()),
                () -> assertNotNull(client.getUpdatedAt())
        );
    }

    @Test
    void fromUpdateDtoToEntity() {
        Client clientUpdated = clientMapper.fromUpdateDtoToEntity(client, updateDto);

        assertAll(
                ()-> assertEquals(id, clientUpdated.getId()),
                ()-> assertEquals(client.getDni(), clientUpdated.getDni()),
                ()-> assertEquals(updateDto.getCompleteName(), clientUpdated.getCompleteName()),
                ()-> assertEquals(updateDto.getEmail(), clientUpdated.getEmail()),
                ()-> assertEquals(client.getPhoneNumber(), clientUpdated.getPhoneNumber()),
                ()-> assertEquals("BARCELONA", clientUpdated.getAddress().getCity()),
                ()-> assertEquals("ANDORRA", clientUpdated.getAddress().getCountry()),
                ()-> assertFalse(clientUpdated.isValidated()),
                ()-> assertFalse(clientUpdated.isDeleted()),
                ()-> assertEquals(client.getCreatedAt(), clientUpdated.getCreatedAt()),
                ()-> assertNotNull(clientUpdated.getUpdatedAt())
        );
    }
}