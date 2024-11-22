package vives.bancovives.rest.clients.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.exceptions.ClientNotFound;
import vives.bancovives.rest.clients.model.Address;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.clients.service.ClientService;
import vives.bancovives.utils.IdGenerator;
import vives.bancovives.utils.PaginationLinksUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    UUID id = UUID.randomUUID();
    String publicId = IdGenerator.generateId();
    Address address = new Address("streetTest","123", "CITYTEST", "PORTUGAL");
    Client client = new Client(id, publicId, "12345678Z", "nameTest",address, "email@test.com", "654321987", null, null, true, false,LocalDateTime.now(), LocalDateTime.now());
    ClientCreateDto createDto = new ClientCreateDto("12345678Z", "nameTest", "email@test.com", "654321987",null,null, "streetTest", "123", "CITYTEST", "PORTUGAL");
    ClientUpdateDto updateDto = ClientUpdateDto.builder().completeName("newNameTest").email("diferent@email.com").city("Barcelona").country("aNdORra").build();
    ClientResponseDto responseDto = new ClientResponseDto(publicId, "12345678Z", "nameTest", "email@test.com", "654321987", null, null, address, true, false, LocalDateTime.now().toString(), LocalDateTime.now().toString());

    ObjectMapper jsonMapper = new ObjectMapper();

    @MockBean
    ClientService clientService;
    @MockBean
    PaginationLinksUtils paginationLinksUtils;
    @Autowired
    MockMvc mockMvc;

    @Autowired
    public ClientControllerTest(ClientService clientService) {
        this.clientService = clientService;
        jsonMapper.registerModule(new JavaTimeModule());
    }

    String endpoint = "/v1/clients";

    @Test
    void getClients() {
    }

    @Test
    void getClientById_Success() throws Exception {
        when(clientService.findById(publicId)).thenReturn(responseDto);
        MockHttpServletResponse response = mockMvc.perform(
                get(endpoint + "/" + id.toString())
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        Client responseClient = jsonMapper.readValue(response.getContentAsString(), Client.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
                () -> assertEquals(client, responseClient),
                () -> assertEquals(client.getId(), responseClient.getId())
        );
        verify(clientService, times(1)).findById(publicId);
    }

    @Test
    void getClientById_NotFound() throws Exception {
        when(clientService.findById(publicId)).thenThrow(new ClientNotFound(id.toString()));
        MockHttpServletResponse response = mockMvc.perform(
                get(endpoint + "/" + id.toString())
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus())
        );
        verify(clientService, times(1)).findById(publicId);
    }

    @Test
    void getClientById_NotValidUUID() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                get(endpoint + "/notValidUUID")
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus())
        );
        verify(clientService, times(0)).findById(publicId);
    }

    @Test
    void createClient() {
    }

    @Test
    void updateClient() {
    }

    @Test
    void deleteClient() {
    }

    @Test
    void validateClient() {
    }
}