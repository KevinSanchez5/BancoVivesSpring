package vives.bancovives.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Spy
    @InjectMocks
    private JwtServiceImpl jwtService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        String privateKeyPath = "src/test/resources/keys/private_key.pem";
        String publicKeyPath = "src/test/resources/keys/public_key.pem";
        ReflectionTestUtils.setField(jwtService, "privateKeyPath", privateKeyPath);
        ReflectionTestUtils.setField(jwtService, "publicKeyPath", publicKeyPath);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600L);
    }

    @Test
    void testGenerateToken() throws Exception {
        // Arrange
        when(userDetails.getUsername()).thenReturn("testUser");

        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        DecodedJWT decodedJWT = JWT.decode(token);
        assertEquals("testUser", decodedJWT.getSubject());
        assertTrue(decodedJWT.getExpiresAt().after(new Date()));
    }

    @Test
    void testIsTokenValid_ValidToken() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("testUser");

        // Act
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_ExpiredToken() throws Exception {
        // Arrange
        when(userDetails.getUsername()).thenReturn("testUser");
        Date pastDate = new Date(System.currentTimeMillis() - (3600 * 1000));
        String expiredToken = jwtService.generateToken(userDetails);
        when(jwtService.extractExpiration(expiredToken)).thenReturn(pastDate);

        // Act
        boolean isValid = jwtService.isTokenValid(expiredToken, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testVerifyToken_InvalidToken() {
        boolean isVerified = jwtService.verifyToken("invalid.token");

        assertFalse(isVerified);
    }
}

