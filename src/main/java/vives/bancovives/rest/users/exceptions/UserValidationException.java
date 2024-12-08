package vives.bancovives.rest.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserValidationException extends UserException {
    public UserValidationException(String message) {
        super(message);
    }
}
