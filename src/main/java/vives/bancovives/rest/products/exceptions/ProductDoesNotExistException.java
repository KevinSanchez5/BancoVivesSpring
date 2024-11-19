package vives.bancovives.rest.products.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductDoesNotExistException extends RuntimeException {
    public ProductDoesNotExistException(String message) {
        super(message);
    }
}
