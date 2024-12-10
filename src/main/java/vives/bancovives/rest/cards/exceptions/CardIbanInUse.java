package vives.bancovives.rest.cards.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CardIbanInUse extends RuntimeException {
    public CardIbanInUse(String message) {
        super(message);
    }
}