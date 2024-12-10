package vives.bancovives.rest.users.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.repositories.UsersRepository;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthUsersServiceImplTest {

    @Mock
    private UsersRepository authUsersRepository;

    @InjectMocks
    private AuthUsersServiceImpl authUsersService;

    @Test
    void testLoadUserByUsername_Success() {
        // Arrange
        String username = "testUser";
        User userDetails = User.builder()
                .username("testUser")
                .password("encodedPassword")
                .roles(Set.of(Role.USER))
                .build();
        when(authUsersRepository.findByUsername(username)).thenReturn(Optional.of(userDetails));

        // Act
        UserDetails user = authUsersService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsername_UsernameNotFound() {
        // Arrange
        String username = "invalidUser";
        when(authUsersRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authUsersService.loadUserByUsername(username);
        });

        assertEquals("Usuario con username invalidUser no encontrado", exception.getMessage());
    }
}
