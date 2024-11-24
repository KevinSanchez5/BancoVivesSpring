package vives.bancovives.rest.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFound extends UserException {
    public UserNotFound(String message) {super(message);
    }

    public UserNotFound(UUID id) {super("Usuario con id " + id + " no encontrado");
    }
}
