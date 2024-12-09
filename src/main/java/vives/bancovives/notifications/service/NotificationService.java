package vives.bancovives.notifications.service;

import vives.bancovives.notifications.model.NotificationType;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.movements.model.Movement;

public interface NotificationService {
    void sendNotificationFromMovement(Movement movement, NotificationType notificationType);
    void sendNotificationFromAccount(Account account, NotificationType notificationType);
    void sendNotificationFromCard(Card card, NotificationType notificationType);
    void sendNotificationFromClient(Client client, NotificationType notificationType);
}