package vives.bancovives.rest.movements.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MovementNotFound extends MovementException {
    public MovementNotFound(String message) {
        super(message);
    }
}
