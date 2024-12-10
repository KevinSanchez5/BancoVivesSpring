package vives.bancovives.rest.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class IncorrectPasswordException extends UserException {
    public IncorrectPasswordException(String message) {
        super(message);
    }
}
