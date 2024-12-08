package vives.bancovives.rest.users.validator;

import org.junit.jupiter.api.Test;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.exceptions.UserValidationException;
import vives.bancovives.rest.users.models.Role;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserUpdateValidatorTest {

    private UserUpdateValidator validator = new UserUpdateValidator();

    @Test
    void validateUpdate_ShouldPass_WithValidAttributes() {
        // Arrange
        UserUpdateDto dto = new UserUpdateDto();
        dto.setUsername("validUser");
        dto.setPassword("validPass");
        dto.setRoles(Set.of(Role.ADMIN));

        // Act & Assert
        assertDoesNotThrow(() -> validator.validateUpdate(dto));
    }

    @Test
    void validateUpdate_ShouldThrow_WhenAllFieldsAreEmpty() {
        // Arrange
        UserUpdateDto dto = new UserUpdateDto();
        dto.setUsername(null);
        dto.setPassword(null);
        dto.setRoles(Collections.emptySet());

        // Act & Assert
        UserValidationException exception = assertThrows(UserValidationException.class, () -> validator.validateUpdate(dto));
        assertEquals("Los campos no pueden estar vacíos", exception.getMessage());
    }

    @Test
    void validateUpdate_ShouldThrow_WhenPasswordTooShort() {
        // Arrange
        UserUpdateDto dto = new UserUpdateDto();
        dto.setUsername("validUser");
        dto.setPassword("123");
        dto.setRoles(Set.of(Role.USER));

        // Act & Assert
        UserValidationException exception = assertThrows(UserValidationException.class, () -> validator.validateUpdate(dto));
        assertEquals("La contraseña debe tener al menos 5 caracteres", exception.getMessage());
    }

    @Test
    void validateUpdate_ShouldThrow_WhenPasswordIsBlank() {
        // Arrange
        UserUpdateDto dto = new UserUpdateDto();
        dto.setUsername("validUser");
        dto.setPassword("   ");
        dto.setRoles(Set.of(Role.USER));

        // Act & Assert
        UserValidationException exception = assertThrows(UserValidationException.class, () -> validator.validateUpdate(dto));
        assertEquals("La contraseña debe tener al menos 5 caracteres", exception.getMessage());
    }

    @Test
    void validateUpdate_ShouldThrow_WhenUsernameIsBlank() {
        // Arrange
        UserUpdateDto dto = new UserUpdateDto();
        dto.setUsername("   ");
        dto.setPassword("validPass");
        dto.setRoles(Set.of(Role.USER));

        // Act & Assert
        UserValidationException exception = assertThrows(UserValidationException.class, () -> validator.validateUpdate(dto));
        assertEquals("El nombre de usuario debe tener al menos 5 caracteres", exception.getMessage());
    }

    @Test
    void validateUpdate_ShouldThrow_WhenRolesAreNull() {
        // Arrange
        UserUpdateDto dto = new UserUpdateDto();
        dto.setUsername("validUser");
        dto.setPassword("validPass");
        dto.setRoles(null);

        // Act & Assert
        UserValidationException exception = assertThrows(UserValidationException.class, () -> validator.validateUpdate(dto));
        assertEquals("Los campos no pueden estar vacíos", exception.getMessage());
    }
}
