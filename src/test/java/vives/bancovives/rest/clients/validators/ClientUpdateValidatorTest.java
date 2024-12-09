package vives.bancovives.rest.clients.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.exceptions.ClientBadRequest;

import static org.junit.jupiter.api.Assertions.*;

class ClientUpdateValidatorTest {

    private ClientUpdateValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new ClientUpdateValidator();
    }

    @Test
    public void testValidateUpdateDto_AllFieldsNull_ThrowsException() {
        ClientUpdateDto dto = new ClientUpdateDto(null, null, null, null, null, null, null, null, null, null, null, null);

        Exception exception = assertThrows(ClientBadRequest.class, () -> {
            validator.validateUpdateDto(dto);
        });

        assertEquals("Debes introducir al menos un campo para actualizar", exception.getMessage());
    }

    @Test
    public void testValidateUpdateDto_ValidDni_NoExceptionThrown() {
        ClientUpdateDto dto = new ClientUpdateDto("12345678Z", null, null, null, null, null, null, null, null, null, null, null);

        assertDoesNotThrow(() -> validator.validateUpdateDto(dto));
    }

    @Test
    public void testValidateUpdateDto_InvalidDni_ThrowsException() {
        ClientUpdateDto dto = new ClientUpdateDto("12345A", null, null, null, null, null, null, null, null, null, null, null);

        Exception exception = assertThrows(ClientBadRequest.class, () -> {
            validator.validateUpdateDto(dto);
        });

        assertEquals("El dni debe de tener 8 digitos y una letra", exception.getMessage());
    }

    @Test
    public void testValidateUpdateDto_InvalidCompleteName_ThrowsException() {
        ClientUpdateDto dto = new ClientUpdateDto(null, "A", null, null, null, null, null, null, null, null, null, null);

        Exception exception = assertThrows(ClientBadRequest.class, () -> {
            validator.validateUpdateDto(dto);
        });

        assertEquals("El nombre tiene que tener entre 5 y 255 caracteres", exception.getMessage());
    }

    @Test
    public void testValidateUpdateDto_ValidCompleteName_NoExceptionThrown() {
        ClientUpdateDto dto = new ClientUpdateDto(null, "John Doe", null, null, null, null, null, null, null, null, null, null);

        assertDoesNotThrow(() -> validator.validateUpdateDto(dto));
    }

    @Test
    public void testValidateUpdateDto_InvalidEmail_ThrowsException() {
        ClientUpdateDto dto = new ClientUpdateDto(null, null, "invalid-email", null, null, null, null, null, null, null, null, null);

        Exception exception = assertThrows(ClientBadRequest.class, () -> {
            validator.validateUpdateDto(dto);
        });

        assertEquals("El email debe de ser valido", exception.getMessage());
    }

    @Test
    public void testValidateUpdateDto_ValidEmail_NoExceptionThrown() {
        ClientUpdateDto dto = new ClientUpdateDto(null, null, "test@example.com", null, null, null, null, null, null, null, null, null);

        assertDoesNotThrow(() -> validator.validateUpdateDto(dto));
    }

    @Test
    public void testValidateUpdateDto_InvalidPhoneNumber_ThrowsException() {
        ClientUpdateDto dto = new ClientUpdateDto(null, null, null, "12345", null, null, null, null, null, null, null, null);

        Exception exception = assertThrows(ClientBadRequest.class, () -> {
            validator.validateUpdateDto(dto);
        });

        assertEquals("El numero de telefono debe de tener 9 digitos y comenzar por 6, 7 o 9", exception.getMessage());
    }

    @Test
    public void testValidateUpdateDto_ValidPhoneNumber_NoExceptionThrown() {
        ClientUpdateDto dto = new ClientUpdateDto(null, null, null, "612345678", null, null, null, null, null, null, null, null);

        assertDoesNotThrow(() -> validator.validateUpdateDto(dto));
    }

    @Test
    public void testValidateUpdateDto_InvalidStreet_ThrowsException() {
        ClientUpdateDto dto = new ClientUpdateDto(null, null, null, null, null, null, "A", null, null, null, null, null);

        Exception exception = assertThrows(ClientBadRequest.class, () -> {
            validator.validateUpdateDto(dto);
        });

        assertEquals("El nombre de la calle tiene que tener entre 5 y 255 caracteres", exception.getMessage());
    }

    @Test
    public void testValidateUpdateDto_ValidStreet_NoExceptionThrown() {
        ClientUpdateDto dto = new ClientUpdateDto(null, null, null, null, null, null, "Calle Mayor 123", null, null, null, null, null);

        assertDoesNotThrow(() -> validator.validateUpdateDto(dto));
    }

    @Test
    public void testValidateUpdateDto_InvalidHouseNumber_ThrowsException() {
        ClientUpdateDto dto = new ClientUpdateDto(null, null, null, null, null, null, null, "ABC", null, null, null, null);

        Exception exception = assertThrows(ClientBadRequest.class, () -> {
            validator.validateUpdateDto(dto);
        });

        assertEquals("El numero de la casa debe ser un numero y puede tener una letra al final de manera opcional", exception.getMessage());
    }

    @Test
    public void testValidateUpdateDto_ValidHouseNumber_NoExceptionThrown() {
        ClientUpdateDto dto = new ClientUpdateDto(null, null, null, null, null, null, null, "123A", null, null, null, null);

        assertDoesNotThrow(() -> validator.validateUpdateDto(dto));
    }
}