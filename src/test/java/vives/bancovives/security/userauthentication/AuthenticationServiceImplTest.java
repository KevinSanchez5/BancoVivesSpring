package vives.bancovives.security.userauthentication;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import vives.bancovives.rest.users.auth.AuthUsersService;
import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.exceptions.UserBadRequest;
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.security.jwt.JwtService;
import vives.bancovives.security.model.JwtAuthResponse;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {

    @Mock
    private AuthUsersService userService;

    @Mock
    private UsersService anotherUsersService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void testSignUp() {
        // Arrange
        UserRequest request = UserRequest.builder()
                .username("username")
                .password("password")
                .build();
        vives.bancovives.rest.users.models.User storedUser = vives.bancovives.rest.users.models.User.builder()
                .username("username")
                .password("encodedPassword")
                .roles(Set.of(Role.USER))
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(anotherUsersService.save(request)).thenReturn(storedUser);
        when(jwtService.generateToken(storedUser)).thenReturn("mockedJwtToken");

        // Act
        JwtAuthResponse response = authenticationService.signUp(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockedJwtToken", response.getToken());
        verify(anotherUsersService).save(request);
    }

    @Test
    void testSignIn_Success() {
        // Arrange
        UserRequest request = UserRequest.builder()
                .username("username")
                .password("password")
                .build();
        User user = User.builder()
                .username("username")
                .password("encodedPassword")
                .roles(Set.of(Role.USER))
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null); // Mock authentication success
        when(userService.loadUserByUsername(request.getUsername())).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("mockedJwtToken");

        // Act
        JwtAuthResponse response = authenticationService.signIn(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockedJwtToken", response.getToken());
        verify(userService).loadUserByUsername(request.getUsername());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testSignIn_Failure_InvalidPassword() {
        // Arrange
        UserRequest request = UserRequest.builder()
                .username("username")
                .password("password")
                .build();
        User user = User.builder()
                .username("username")
                .password("encodedPassword")
                .roles(Set.of(Role.USER))
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null); // Mock authentication success
        when(userService.loadUserByUsername(request.getUsername())).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        UserBadRequest exception = assertThrows(UserBadRequest.class, () -> {
            authenticationService.signIn(request);
        });
        assertEquals("La contraseña no es correcta", exception.getMessage());
    }

    @Test
    void testSignIn_Failure_UserNotFound() {
        // Arrange
        UserRequest request = UserRequest.builder()
                .username("username")
                .password("password")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null); // Mock authentication success
        when(userService.loadUserByUsername(request.getUsername())).thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.signIn(request));
    }
}