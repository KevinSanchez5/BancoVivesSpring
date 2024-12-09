package vives.bancovives.rest.users.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.dockerjava.api.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.rest.users.mappers.UsersMapper;
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.security.model.JwtAuthResponse;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;

    @MockBean
    private PaginationLinksUtils paginationLinksUtils;

    @MockBean
    private UsersMapper usersMapper;

    private final ObjectMapper mapper = new ObjectMapper();

    // Test data
    private UserResponse userResponse;
    private JwtAuthResponse jwtAuthResponse;
    private User user;
    private UserRequest userRequest;
    private UserUpdateDto userUpdateDto;
    private Page<User> userPage;
    private String userId = "12345";

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());

        // Set up sample data
        userResponse = UserResponse.builder()
                .username("testuser")
                .build();

        userRequest = UserRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        userUpdateDto = UserUpdateDto.builder()
                .username("updateduser")
                .build();

        user = User.builder()
                .username("testuser")
                .password("password")
                .build();

        jwtAuthResponse = JwtAuthResponse.builder().token("token").build();

        doNothing().when(usersService).deleteById(anyString());

        userPage = new PageImpl<>(List.of(user));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void findAllUsers() throws Exception {
        // Arrange
        when(usersService.findAll(any(), any(), any(Pageable.class)))
                .thenReturn(userPage);
        when(usersMapper.fromEntityToResponseDto(any()))
                .thenReturn(userResponse);
        when(paginationLinksUtils.createLinkHeader(any(), any()))
                .thenReturn("link");

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        get("/v1/users")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        PageResponse<UserResponse> pageResponse = mapper.readValue(response.getContentAsString(), new TypeReference<>() {});
        assertAll(
                () -> assertEquals(1, pageResponse.content().size()),
                () -> assertEquals("testuser", pageResponse.content().get(0).getUsername())
        );
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    void findUserById() throws Exception {
        // Arrange
        when(usersService.findById(userId))
                .thenReturn(user);
        when(usersMapper.fromEntityToResponseDto(any()))
                .thenReturn(userResponse);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        get("/v1/users/" + userId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        UserResponse responseBody = mapper.readValue(response.getContentAsString(), UserResponse.class);
        assertEquals(userResponse.getId(), responseBody.getId());
    }

    @Test
    @WithMockUser(username = "user", roles = {"SUPER_ADMIN"})
    void addAdmin() throws Exception {
        // Arrange
        when(usersService.save(any(UserRequest.class)))
                .thenReturn(user);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        post("/v1/users/addAdmin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
    }
    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    void addAdmin_shouldThrowUnauthorizedException() throws Exception {
        // Arrange
        when(usersService.save(any(UserRequest.class)))
                .thenReturn(user);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        post("/v1/users/addAdmin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        assertEquals(403, response.getStatus());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    void updateUser() throws Exception {
        // Arrange
        when(usersService.update(anyString(), any(UserUpdateDto.class)))
                .thenReturn(user);
        when(usersMapper.fromEntityToResponseDto(any()))
                .thenReturn(userResponse);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        put("/v1/users/" + userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userUpdateDto))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        UserResponse responseBody = mapper.readValue(response.getContentAsString(), UserResponse.class);
        assertEquals(userResponse.getUsername(), responseBody.getUsername());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    void deleteAdmin() throws Exception {
        // Arrange
        user.setRoles(Set.of(Role.ADMIN));
        when(usersService.findById(anyString())).thenReturn(user);
        doNothing().when(usersService).deleteById(anyString());

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        delete("/v1/users/" + userId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        assertEquals(204, response.getStatus());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    void itShouldNotLetYouDeleteRegularUsers() throws Exception {
        // Arrange
        user.setRoles(Set.of(Role.USER));
        when(usersService.findById(anyString())).thenReturn(user);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        delete("/v1/users/" + userId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        assertEquals(403, response.getStatus());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    void itShouldNotLetYouDeleteSuperAdmins() throws Exception {
        // Arrange
        user.setRoles(Set.of(Role.SUPER_ADMIN));
        when(usersService.findById(anyString())).thenReturn(user);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        delete("/v1/users/" + userId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        assertEquals(403, response.getStatus());
    }

    @Test
    void signIn() throws Exception {
        // Arrange
        JwtAuthResponse authResponse = JwtAuthResponse.builder()
                .token("dummyToken")
                .build();
        when(usersService.signIn(any(UserRequest.class)))
                .thenReturn(authResponse);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        post("/v1/users/signIn")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        JwtAuthResponse responseBody = mapper.readValue(response.getContentAsString(), JwtAuthResponse.class);
        assertEquals("dummyToken", responseBody.getToken());
    }

    @Test
    void signIn_ShouldThrowUnauthorizedException() throws Exception {
        // Arrange
        when(usersService.signIn(any(UserRequest.class)))
                .thenThrow( new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        post("/v1/users/signIn")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userRequest))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Assert
        assertEquals(401, response.getStatus());
    }
}
