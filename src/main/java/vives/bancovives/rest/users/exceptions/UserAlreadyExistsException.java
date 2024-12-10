package vives.bancovives.rest.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException(String message) {super(message);
    }
}
