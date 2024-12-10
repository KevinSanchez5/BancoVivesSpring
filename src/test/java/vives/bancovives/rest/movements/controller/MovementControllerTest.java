package vives.bancovives.rest.movements.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.types.ObjectId;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.exceptions.ClientBadRequest;
import vives.bancovives.rest.clients.exceptions.ClientNotFound;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.output.MovementResponseDto;
import vives.bancovives.rest.movements.exceptions.MovementBadRequest;
import vives.bancovives.rest.movements.exceptions.MovementNotFound;
import vives.bancovives.rest.movements.model.Movement;
import vives.bancovives.rest.movements.model.MovementType;
import vives.bancovives.rest.movements.services.MovementService;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class MovementControllerTest {

    ObjectId id = new ObjectId();
    Account accountOfReference = Account.builder().publicId("123").iban("BE68539007547034").balance(500).build();
    Account accountOfDestination = Account.builder().publicId("456").iban("BE68539007547035").balance(10).build();
    MovementCreateDto movementCreateDto = MovementCreateDto.builder().movementType("TRANSFERENCIA").ibanOfReference("BE68539007547034").ibanOfDestination("BE68539007547035").amount(100.0).build(); ;
    Movement movement = Movement.builder().id(id).movementType(MovementType.TRANSFERENCIA).amountOfMoney(100.0).accountOfDestination(accountOfDestination).accountOfReference(accountOfReference).build();
    MovementResponseDto movementResponseDto = MovementResponseDto.builder().id(movement.get_id()).movementType("TRANSFERENCIA").amountMoved(100.0).build();
    Principal principal = () -> "admin";
    Principal principalUser = () -> "user";

    ObjectMapper jsonMapper = new ObjectMapper();

    @MockBean
    MovementService movementService;

    @MockBean
    PaginationLinksUtils paginationLinksUtils;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    public MovementControllerTest(MovementService movementService) {
        this.movementService = movementService;
        jsonMapper.registerModule(new JavaTimeModule());
    }

    String endpoint = "/v1/movements";

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllMovements() throws Exception{
        List<MovementResponseDto> list = List.of(movementResponseDto);
        Page<MovementResponseDto> page = new PageImpl<>(list);
        when(movementService.findAll(any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        MockHttpServletResponse response = mockMvc.perform(
                        get(endpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResponse<MovementResponseDto> pageResponse = jsonMapper.readValue(response.getContentAsString(), new TypeReference<PageResponse<MovementResponseDto>>() {});
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(1, pageResponse.content().size())
        );
        verify(movementService).findAll(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getMovementById() throws Exception{
        when(movementService.findById(id)).thenReturn(movementResponseDto);
        MockHttpServletResponse response = mockMvc.perform(
                        get(endpoint + "/" + id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        MovementResponseDto responseM = jsonMapper.readValue(response.getContentAsString(), MovementResponseDto.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
                () -> assertEquals(movementResponseDto, responseM),
                () -> assertEquals(movement.get_id(), responseM.getId()),
                () -> assertEquals(movement.getMovementType().toString(), responseM.getMovementType()),
                () -> assertEquals(movement.getAmountOfMoney(), responseM.getAmountMoved())
        );
        verify(movementService, times(1)).findById(id);
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getMovementById_NOTFOUND() throws Exception{
        when(movementService.findById(id)).thenThrow(new MovementNotFound(id.toHexString()));
        MockHttpServletResponse response = mockMvc.perform(
                        get(endpoint + "/" + id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus())
        );
        verify(movementService, times(1)).findById(id);
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateMovement() throws Exception{
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateMovement_NotFound() throws Exception{
        when(movementService.update(id, movementCreateDto)).thenThrow(new MovementNotFound(id.toHexString()));

        MockHttpServletResponse response = mockMvc.perform(
                put(endpoint + "/" + id)
                        .content(jsonMapper.writeValueAsString(movementCreateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus())
        );

        verify(movementService, times(1)).update(id, movementCreateDto);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateMovement_BadRequest() throws Exception{
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteMovement() throws Exception{
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteMovement_BadRequest() throws Exception{
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void addInterest() throws Exception{
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void addInterest_Forbidden() throws Exception{
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void findMe() throws Exception{
    }
}