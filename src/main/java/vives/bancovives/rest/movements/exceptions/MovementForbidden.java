package vives.bancovives.rest.movements.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MovementForbidden extends MovementException {
    public MovementForbidden(String message) {
        super(message);
    }
}
