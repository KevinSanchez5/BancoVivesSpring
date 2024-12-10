package vives.bancovives.notifications.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class NotificationSenderException extends RuntimeException {
    public NotificationSenderException(String message) {
        super(message);
    }
}
