package vives.bancovives.rest.movements.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.output.MovementResponseDto;
import vives.bancovives.rest.movements.model.Movement;
import vives.bancovives.rest.movements.services.MovementService;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class MovementControllerTest {

    ObjectId id = new ObjectId();
    Account accountOfReference;
    Account accountOfDestination;
    MovementCreateDto movementCreateDto;
    Movement movement;
    MovementResponseDto movementResponseDto = MovementResponseDto.builder().movementType("TRANSFERENCIA").amountMoved(100.0).build();

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
        PageResponse<MovementResponseDto> pageResponse = jsonMapper.readValue(response.getContentAsString(), PageResponse.class);
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(1, pageResponse.content().size()),
                () -> assertEquals(id.toHexString(), pageResponse.content().get(0).getId())
        );
        verify(movementService).findAll(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getMovementById() {
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getMovementById_NOTFOUND() {
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createMovement() {
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createMovement_BadRequest() {
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateMovement() {
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateMovement_NotFound() {
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateMovement_BadRequest() {
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteMovement() {
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteMovement_NotFound() {
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteMovement_BadRequest() {
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void addInterest() {
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void addInterest_Forbidden() {
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void findMe() {
    }
}