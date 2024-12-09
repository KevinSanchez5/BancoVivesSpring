package vives.bancovives.notifications.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Builder.Default
    private String id = IdGenerator.generateId();
    private User recipient;
    private NotificationType type;
    private String message;
    @Builder.Default
    private String createdAt = LocalDateTime.now().toString();
}

