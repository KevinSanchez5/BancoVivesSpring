package vives.bancovives.rest.accounts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AccountConflictException extends RuntimeException {
    public AccountConflictException(String message) {
        super(message);
    }
}
