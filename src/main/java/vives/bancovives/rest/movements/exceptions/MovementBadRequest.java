package vives.bancovives.rest.movements.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MovementBadRequest extends MovementException {
    public MovementBadRequest(String message) {
        super(message);
    }
}
