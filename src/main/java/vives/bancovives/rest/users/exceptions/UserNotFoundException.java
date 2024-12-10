package vives.bancovives.rest.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends UserException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
