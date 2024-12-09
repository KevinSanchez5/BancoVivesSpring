package vives.bancovives.security.filter;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.security.jwt.JwtService;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader() throws Exception {
        // Arrange
        request.setRequestURI("/some-uri");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_InvalidToken() throws Exception {
        // Arrange
        String invalidToken = "Bearer invalid_token";
        request.addHeader("Authorization", invalidToken);

        when(jwtService.extractUserName(invalidToken.substring(7))).thenThrow(new RuntimeException("Invalid token"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void testDoFilterInternal_ValidToken() throws Exception {
        // Arrange
        String validToken = "Bearer valid_token";
        request.addHeader("Authorization", validToken);

        String username = "testUser";
        User userDetails = User.builder()
                .username("testUser")
                .password("encodedPassword")
                .roles(Set.of(Role.USER))
                .build();

        when(jwtService.extractUserName(validToken.substring(7))).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken.substring(7), userDetails)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_UserNotFound() throws Exception {
        // Arrange
        String validToken = "Bearer valid_token";
        request.addHeader("Authorization", validToken);

        String username = "testUser";
        UserDetails userDetails = User.builder()
                .username("username")
                .password("encodedPassword")
                .roles(Set.of(Role.USER))
                .build();

        when(jwtService.extractUserName(validToken.substring(7))).thenReturn(username);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void testDoFilterInternal_TokenInvalid() throws Exception {
        // Arrange
        String validToken = "Bearer valid_token";
        request.addHeader("Authorization", validToken);

        String username = "testUser";
        User userDetails = User.builder()
                .username("username")
                .password("encodedPassword")
                .roles(Set.of(Role.USER))
                .build();

        when(jwtService.extractUserName(validToken.substring(7))).thenReturn(username);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
}
