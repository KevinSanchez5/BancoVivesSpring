package vives.bancovives.rest.products.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductAlreadyExistsException extends ProductException {
    public ProductAlreadyExistsException(String message) {
        super(message);
    }
}
