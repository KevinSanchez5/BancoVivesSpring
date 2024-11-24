package vives.bancovives.rest.cards.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CardAlreadyExistsException extends CardException {
    public CardAlreadyExistsException(String message) {
        super(message);
    }
}
