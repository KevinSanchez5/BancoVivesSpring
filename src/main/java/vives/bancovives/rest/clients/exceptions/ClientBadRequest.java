package vives.bancovives.rest.clients.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ClientBadRequest extends ClientException {
    public ClientBadRequest(String message) {
        super(message);
    }
}
