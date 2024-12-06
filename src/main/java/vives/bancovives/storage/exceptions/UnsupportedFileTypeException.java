package vives.bancovives.storage.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
public class UnsupportedFileTypeException extends StorageException {
    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}
