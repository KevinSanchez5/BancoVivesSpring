package vives.bancovives.rest.clients.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ClientConflict extends ClientException {
    public ClientConflict(String message) {
        super(message);
    }
}
