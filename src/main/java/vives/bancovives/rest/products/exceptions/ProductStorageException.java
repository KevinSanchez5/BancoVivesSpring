package vives.bancovives.rest.products.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductStorageException extends ProductException {
    public ProductStorageException(String message) {
        super(message);
    }
}
