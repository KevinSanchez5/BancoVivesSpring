package vives.bancovives.rest.clients.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.exceptions.ClientBadRequest;
import vives.bancovives.rest.clients.exceptions.ClientConflict;
import vives.bancovives.rest.clients.exceptions.ClientNotFound;
import vives.bancovives.rest.clients.model.Address;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.clients.service.ClientService;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.utils.IdGenerator;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    UUID id = UUID.randomUUID();
    String publicId = IdGenerator.generateId();
    Address address = new Address("streetTest","123", "CITYTEST", "PORTUGAL");
    User user = new User(id, publicId, "usernameTest", "passwordTest", Collections.singleton(Role.USER), null, LocalDateTime.now(), LocalDateTime.now(), false);
    Client client = new Client(id, publicId, "12345678Z", "nameTest",address, "email@test.com", "654321987", null, null, user, true, false,LocalDateTime.now(), LocalDateTime.now());
    ClientCreateDto createDto = new ClientCreateDto("12345678Z", "nameTest", "email@test.com", "654321987",null,null, "streetTest", "123", "CITYTEST", "PORTUGAL", "usernameTest", "passwordTest");
    ClientUpdateDto updateDto = ClientUpdateDto.builder().completeName("newNameTest").email("diferent@email.com").city("Barcelona").country("aNdORra").build();
    UserResponse userResponse = new UserResponse(publicId, "usernameTest", Collections.singleton(Role.USER), false);
    ClientResponseDto responseDto = new ClientResponseDto(publicId, "12345678Z", "nameTest", "email@test.com", "654321987", null, null, address, userResponse,true, false, LocalDateTime.now().toString(), LocalDateTime.now().toString());

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
    void getClients() throws Exception {
        List<ClientResponseDto> list = List.of(responseDto);
        Page<ClientResponseDto> page = new PageImpl<>(list);
        when(clientService.findAll(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        MockHttpServletResponse response = mockMvc.perform(
                get(endpoint)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        PageResponse<ClientResponseDto> responseMapped = jsonMapper.readValue(response.getContentAsString(), new TypeReference<PageResponse<ClientResponseDto>>() {});
        assertAll(
                () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
                () -> assertEquals(page.getTotalElements(), responseMapped.content().size()),
                () -> assertEquals(responseDto, responseMapped.content().getFirst())
        );

        verify(clientService, times(1)).findAll(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getClientById_Success() throws Exception {
        when(clientService.findById(publicId)).thenReturn(responseDto);
        MockHttpServletResponse response = mockMvc.perform(
                get(endpoint + "/" + publicId)
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        ClientResponseDto responseClient = jsonMapper.readValue(response.getContentAsString(), ClientResponseDto.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
                () -> assertEquals(responseDto, responseClient),
                () -> assertEquals(client.getPublicId(), responseClient.getPublicId())
        );
        verify(clientService, times(1)).findById(publicId);
    }

    @Test
    void getClientById_NotFound() throws Exception {
        when(clientService.findById("123")).thenThrow(new ClientNotFound("123"));
        MockHttpServletResponse response = mockMvc.perform(
                get(endpoint + "/" + "123")
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus())
        );
        verify(clientService, times(1)).findById("123");
    }


    @Test
    void createClient() throws Exception{
        when(clientService.save(createDto)).thenReturn(responseDto);

        MockHttpServletResponse response = mockMvc.perform(
                post(endpoint)
                        .content(jsonMapper.writeValueAsString(createDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andReturn().getResponse();

        ClientResponseDto responseClient = jsonMapper.readValue(response.getContentAsString(), ClientResponseDto.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
                () -> assertEquals(responseDto, responseClient),
                () -> assertEquals(client.getPublicId(), responseClient.getPublicId()),
                () -> assertEquals(client.getDni(), responseClient.getDni())
        );

        verify(clientService, times(1)).save(createDto);
    }

    @Test
    void createClient_BadRequest() throws Exception{
        when(clientService.save(createDto)).thenThrow(new ClientBadRequest(""));

        MockHttpServletResponse response = mockMvc.perform(
                post(endpoint)
                        .content(jsonMapper.writeValueAsString(createDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus())
        );

        verify(clientService, times(1)).save(createDto);
    }

    @Test
    void createClient_Conflict() throws Exception{
        when(clientService.save(createDto)).thenThrow(new ClientConflict(""));

        MockHttpServletResponse response = mockMvc.perform(
                post(endpoint)
                        .content(jsonMapper.writeValueAsString(createDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.CONFLICT.value(), response.getStatus())
        );

        verify(clientService, times(1)).save(createDto);

    }

    @Test
    void updateClient() throws Exception{
        when(clientService.update(publicId, updateDto)).thenReturn(responseDto);

        MockHttpServletResponse response = mockMvc.perform(
                put(endpoint + "/" + publicId)
                        .content(jsonMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        ClientResponseDto responseClient = jsonMapper.readValue(response.getContentAsString(), ClientResponseDto.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
                () -> assertEquals(responseDto, responseClient),
                () -> assertEquals(client.getPublicId(), responseClient.getPublicId()),
                () -> assertEquals(client.getDni(), responseClient.getDni())
        );

        verify(clientService, times(1)).update(publicId, updateDto);
    }

    @Test
    void updateClient_NotFound() throws Exception{
        when(clientService.update("123", updateDto)).thenThrow(new ClientNotFound("123"));

        MockHttpServletResponse response = mockMvc.perform(
                put(endpoint + "/" + "123")
                        .content(jsonMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus())
        );

        verify(clientService, times(1)).update("123", updateDto);
    }

    @Test
    void updateClient_BadRequest() throws Exception{
        when(clientService.update(publicId, updateDto)).thenThrow(new ClientBadRequest(""));

        MockHttpServletResponse response = mockMvc.perform(
                put(endpoint + "/" + publicId)
                        .content(jsonMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus())
        );

        verify(clientService, times(1)).update(publicId, updateDto);
    }

    @Test
    void updateClient_Conflict() throws Exception{
        when(clientService.update(publicId, updateDto)).thenThrow(new ClientConflict(""));

        MockHttpServletResponse response = mockMvc.perform(
                put(endpoint + "/" + publicId)
                        .content(jsonMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.CONFLICT.value(), response.getStatus())
        );

        verify(clientService, times(1)).update(publicId, updateDto);

    }

    @Test
    void deleteClient() throws Exception{
        when(clientService.deleteByIdLogically(publicId, Optional.of(false))).thenReturn(responseDto);

        MockHttpServletResponse response = mockMvc.perform(
                delete(endpoint + "/" + publicId )
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.OK.value(), response.getStatus())
        );

        verify(clientService, times(1)).deleteByIdLogically(publicId, Optional.of(false));
    }

    @Test
    void deleteClient_NotFound() throws Exception{
        when(clientService.deleteByIdLogically("123", Optional.of(false))).thenThrow(new ClientNotFound("123"));

        MockHttpServletResponse response = mockMvc.perform(
                delete(endpoint + "/" + "123")
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus())
        );

        verify(clientService, times(1)).deleteByIdLogically("123", Optional.of(false));
    }

    @Test
    void validateClient() throws Exception{
        when(clientService.validateClient(publicId)).thenReturn(responseDto);

        MockHttpServletResponse response = mockMvc.perform(
                put(endpoint + "/" + publicId + "/validate")
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        ClientResponseDto responseClient = jsonMapper.readValue(response.getContentAsString(), ClientResponseDto.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
                () -> assertEquals(responseDto, responseClient),
                () -> assertEquals(client.getPublicId(), responseClient.getPublicId()),
                () -> assertEquals(client.getDni(), responseClient.getDni())
        );

        verify(clientService, times(1)).validateClient(publicId);
    }

    @Test
    void validateClient_NotFound() throws Exception{
        when(clientService.validateClient("123")).thenThrow(new ClientNotFound("123"));

        MockHttpServletResponse response = mockMvc.perform(
                put(endpoint + "/" + "123" + "/validate")
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus())
        );

        verify(clientService, times(1)).validateClient("123");
    }
}