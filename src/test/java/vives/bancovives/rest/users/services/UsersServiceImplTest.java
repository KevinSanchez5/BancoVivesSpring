package vives.bancovives.rest.users.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import vives.bancovives.rest.users.auth.AuthUsersService;
import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.exceptions.IncorrectPasswordException;
import vives.bancovives.rest.users.exceptions.UserAlreadyExistsException;
import vives.bancovives.rest.users.exceptions.UserNotFoundException;
import vives.bancovives.rest.users.mappers.UsersMapper;
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.repositories.UsersRepository;
import vives.bancovives.rest.users.validator.UserUpdateValidator;
import vives.bancovives.security.jwt.JwtService;
import vives.bancovives.security.model.JwtAuthResponse;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceImplTest {

    @Mock
    private AuthUsersService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UsersRepository usersRepository;
    @Mock private UsersMapper usersMapper;
    @Mock private UserUpdateValidator userUpdateValidator;

    @InjectMocks
    private UsersServiceImpl usersService;

    @Test
    void findAll_shouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Specification<User> anySpec = any();
        Page<User> mockPage = new PageImpl<>(List.of(new User()));
        when(usersRepository.findAll(anySpec, eq(pageable))).thenReturn(mockPage);


        // Act
        Page<User> result = usersService.findAll(Optional.empty(), Optional.empty(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findById_shouldReturnUser() {
        // Arrange
        String publicId = "public-id";
        User mockUser = new User();
        when(usersRepository.findByPublicId(publicId)).thenReturn(Optional.of(mockUser));

        // Act
        User result = usersService.findById(publicId);

        // Assert
        assertNotNull(result);
        verify(usersRepository).findByPublicId(publicId);
    }

    @Test
    void findById_shouldThrowExceptionIfUserNotFound() {
        // Arrange
        String publicId = "non-existent-id";
        when(usersRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> usersService.findById(publicId));
    }

    @Test
    void save_shouldCreateNewUserAndReturnToken() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("testUser");
        request.setPassword("password");

        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .roles(Set.of(Role.ADMIN))
                .publicId(IdGenerator.generateId())
                .build();

        when(usersMapper.fromRequestDtotoUser(request)).thenReturn(mockUser);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(usersRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        JwtAuthResponse response = usersService.save(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(usersRepository).save(any(User.class));
    }

    @Test
    void update_shouldUpdateUser() {
        // Arrange
        String publicId = "public-id";
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("updatedUser");
        User oldUser = new User();
        User updatedUser = new User();

        when(usersRepository.findByPublicId(publicId)).thenReturn(Optional.of(oldUser));
        when(usersMapper.fromUpdateDtotoUser(oldUser, updateDto)).thenReturn(updatedUser);
        when(usersRepository.save(updatedUser)).thenReturn(updatedUser);

        // Act
        User result = usersService.update(publicId, updateDto);

        // Assert
        assertNotNull(result);
        verify(usersRepository).save(updatedUser);
    }

    @Test
    void deleteById_shouldRemoveUser() {
        // Arrange
        String publicId = "public-id";
        User userToDelete = new User();
        UUID id = UUID.randomUUID();
        userToDelete.setId(id);
        when(usersRepository.findByPublicId(publicId)).thenReturn(Optional.of(userToDelete));

        // Act
        usersService.deleteById(publicId);

        // Assert
        verify(usersRepository).deleteById(id);
    }

    @Test
    void signIn_shouldAuthenticateAndReturnToken() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("testUser");
        request.setPassword("password");

        User mockUser = new User();
        mockUser.setPassword("encodedPassword");
        when(userService.loadUserByUsername(request.getUsername())).thenReturn(mockUser);
        when(passwordEncoder.matches(request.getPassword(), "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(mockUser)).thenReturn("jwt-token");

        // Act
        JwtAuthResponse response = usersService.signIn(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void signIn_shouldThrowIncorrectPasswordException() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("testUser");
        request.setPassword("wrongPassword");

        User mockUser = new User();
        mockUser.setPassword("encodedPassword");
        when(userService.loadUserByUsername(request.getUsername())).thenReturn(mockUser);
        when(passwordEncoder.matches(request.getPassword(), "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(IncorrectPasswordException.class, () -> usersService.signIn(request));
    }

    @Test
    void saveUserFromClient() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("uniqueUsername");
        Mockito.when(usersRepository.save(newUser)).thenReturn(newUser);

        // Act
        User result = usersService.saveUserFromClient(newUser);

        // Assert
        assertNotNull(result);
        assertEquals(newUser, result);
        verify(usersRepository).save(newUser);
    }

    @Test
    void saveUserFromClient_ShouldThrowExceptionWhenUsernameIsTaken() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("duplicateUsername");
        when(usersRepository.findByUsernameEqualsIgnoreCase("duplicateUsername")).thenReturn(Optional.of(newUser));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> usersService.saveUserFromClient(newUser)
        );

        assertEquals("Ya existe un usuario con ese nombre de usuario", exception.getMessage());
        verify(usersRepository).findByUsernameEqualsIgnoreCase("duplicateUsername");
    }

    @Test
    void updateUserFromClient() {
        // Arrange
        String publicId = "1234";
        User existingUser = new User();
        existingUser.setUsername("oldUsername");

        User updateUser = new User();
        updateUser.setUsername("newUsername");
        updateUser.setUpdatedAt(LocalDateTime.now());

        when(usersRepository.findByPublicId(publicId)).thenReturn(Optional.of(existingUser));
        when(usersMapper.updateUserFromClient(existingUser, updateUser)).thenReturn(updateUser);
        when(usersRepository.save(updateUser)).thenReturn(updateUser);

        // Act
        User result = usersService.updateUserFromClient(publicId, updateUser);

        // Assert
        assertNotNull(result);
        assertEquals(updateUser, result);
        verify(usersMapper).updateUserFromClient(existingUser, updateUser);
        verify(usersRepository).save(updateUser);
    }

    @Test
    void updateUserFromClient_ShouldThrowException_WhenPublicIdNotFound() {
        // Arrange
        String publicId = "invalidPublicId";
        User updateUser = new User();
        updateUser.setUsername("newUsername");

        when(usersRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> usersService.updateUserFromClient(publicId, updateUser)
        );

        verify(usersRepository).findByPublicId(publicId);
    }

    @Test
    void findUserByUsername_ShouldReturnUser_WhenUsernameExists() {
        // Arrange
        String username = "existingUser";
        User existingUser = new User();
        existingUser.setUsername(username);

        when(usersRepository.findByUsernameEqualsIgnoreCase(username)).thenReturn(Optional.of(existingUser));

        // Act
        User result = usersService.findUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(existingUser, result);
        verify(usersRepository).findByUsernameEqualsIgnoreCase(username);
    }

    @Test
    void findUserByUsername_ShouldThrowException_WhenUsernameNotFound() {
        // Arrange
        String username = "nonExistentUser";
        when(usersRepository.findByUsernameEqualsIgnoreCase(username)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> usersService.findUserByUsername(username)
        );

        assertEquals("El usuario con el nombre de usuario: nonExistentUser no existe", exception.getMessage());
        verify(usersRepository).findByUsernameEqualsIgnoreCase(username);
    }

}
