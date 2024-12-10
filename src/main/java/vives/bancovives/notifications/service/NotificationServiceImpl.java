package vives.bancovives.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vives.bancovives.notifications.exceptions.NotificationSenderException;
import vives.bancovives.notifications.model.Notification;
import vives.bancovives.notifications.model.NotificationType;
import vives.bancovives.notifications.websocket.config.WebSocketConfig;
import vives.bancovives.notifications.websocket.config.WebSocketHandler;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.service.AccountService;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.service.CardService;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.clients.service.ClientService;
import vives.bancovives.rest.movements.model.Movement;
import vives.bancovives.rest.movements.model.MovementType;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.services.UsersService;

import java.io.IOException;

/**
 * Clase de implementación de {@link NotificationService} que gestiona el envío
 * de notificaciones a los usuarios basándose en movimientos, cuentas, tarjetas y clientes
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService{

    private final ClientService clientService;
    private final UsersService usersService;
    private final WebSocketHandler webSocketHandler;
    private final WebSocketConfig config;

    @Autowired
    public NotificationServiceImpl(
            ClientService clientService,
            CardService cardService,
            AccountService accountService,
            UsersService usersService,
            WebSocketConfig config
    ) {
        this.clientService = clientService;
        this.usersService = usersService;
        this.config = config;
        this.webSocketHandler = config.webSocketNotificationsHandler();
    }


    /**
     * Envía una notificación basada en el movimiento y el tipo de notificación.
     *
     * @param movement El movimiento que desencadenó la notificación.
     * @param notificationType El tipo de notificación a enviar.
     *
     * Esta función verifica el tipo de notificación y llama al método de envío de notificación correspondiente
     * (crear, actualizar o eliminar).
     */
    @Override
    public void sendNotificationFromMovement(Movement movement, NotificationType notificationType) {
        if (notificationType == NotificationType.CREATE){
            sendCreateNotification(movement);
        }
        else if (notificationType == NotificationType.UPDATE){
            sendUpdateNotification(movement);
        }
        else if (notificationType == NotificationType.DELETE){
            sendDeleteNotification(movement);
        }
    }

    /**
     * Envia una notificación de eliminación cuando se realiza una transferencia.
     *
     * @param movement El movimiento que desencadenó la notificación de eliminación. Este movimiento debe ser de tipo
     *                 {@link MovementType#TRANSFERENCIA}.
     *
     * La función verifica si el movimiento es de tipo {@link MovementType#TRANSFERENCIA}. Si es así, obtiene los datos
     * de los clientes y usuarios involucrados en la transferencia y envía una notificación de eliminación a cada uno de
     * ellos. Si se produce alguna excepción durante el proceso, se registra un mensaje de error y se lanza una excepción
     * {@link NotificationSenderException}.
     */
    private void sendDeleteNotification(Movement movement) {
        if (movement.getMovementType() == MovementType.TRANSFERENCIA){
            Account accountOfReference = movement.getAccountOfReference();
            Account accountOfDestination = movement.getAccountOfDestination();
            ClientResponseDto clientOfReference = clientService.findById(accountOfReference.getClient().getPublicId());
            ClientResponseDto clientOfDestination = clientService.findById(accountOfDestination.getClient().getPublicId());
            User firstUser = usersService.findById(clientOfReference.getUserResponse().getId());
            User secondUser = usersService.findById(clientOfDestination.getUserResponse().getId());
            
            try {
                Notification notification = Notification.builder()
                        .recipient(firstUser)
                        .message("Se ha eliminado una transferencia")
                        .type(NotificationType.DELETE)
                        .build();
                sendNotificationToClient(notification, firstUser);
                notification.setRecipient(secondUser);
                sendNotificationToClient(notification, secondUser);
            }catch (Exception e) {
                log.error("Error al enviar notificación: {}", e.getMessage());
                throw new NotificationSenderException("Error al enviar notificación: " + e);
            }
        }
    }

    /**
     * Envía una notificación de actualización basada en el tipo de movimiento.
     *
     * @param movement El movimiento que desencadenó la notificación.
     *
     * Esta función verifica el tipo de movimiento y llama al método de envío de notificación correspondiente
     * (crear, actualizar o eliminar). Si el movimiento es una transferencia, recupera los datos de los clientes y usuarios
     * involucrados en la transferencia y envía una notificación de actualización a cada uno de ellos. Si se produce una excepción durante el
     * proceso, registra un mensaje de error y lanza una {@link NotificationSenderException}.
     */
    private void sendUpdateNotification(Movement movement) {
        if (movement.getMovementType() == MovementType.TRANSFERENCIA){
            Account accountOfReference = movement.getAccountOfReference();
            Account accountOfDestination = movement.getAccountOfDestination();
            ClientResponseDto clientOfReference = clientService.findById(accountOfReference.getClient().getPublicId());
            ClientResponseDto clientOfDestination = clientService.findById(accountOfDestination.getClient().getPublicId());
            User firstUser = usersService.findById(clientOfReference.getUserResponse().getId());
            User secondUser = usersService.findById(clientOfDestination.getUserResponse().getId());

            try {
                Notification notification = Notification.builder()
                        .recipient(firstUser)
                        .message("Se actualizó una transferencia")
                        .type(NotificationType.UPDATE)
                        .build();
                sendNotificationToClient(notification, firstUser);
                notification.setRecipient(secondUser);
                sendNotificationToClient(notification, secondUser);
            }catch (Exception e) {
                log.error("Error al enviar notificación: {}", e.getMessage());
                throw new NotificationSenderException("Error al enviar notificación: " + e);
            }
        }else {
            Account accountOfReference = movement.getAccountOfReference();
            ClientResponseDto clientOfReference = clientService.findById(accountOfReference.getClient().getPublicId());
            User firstUser = usersService.findById(clientOfReference.getUserResponse().getId());

            try {
                Notification notification = Notification.builder()
                        .recipient(firstUser)
                        .message("Se ha realizado un movimiento en su cuenta bancaria")
                        .type(NotificationType.CREATE)
                        .build();
                sendNotificationToClient(notification, firstUser);
            } catch (Exception e) {
                log.error("Error al enviar notificación: {}", e.getMessage());
                throw new NotificationSenderException("Error al enviar notificación: " + e);
            }
        }
    }
    /**
     * Envía una notificación de creación basada en el movimiento y el tipo de movimiento.
     *
     * @param movement El movimiento que desencadenó la notificación.
     *
     * Esta función verifica el tipo de movimiento y llama al método de notificación de envío correspondiente
     * (crear, actualizar o eliminar). Si el movimiento es una transferencia, recupera los datos de los clientes y usuarios
     * involucrados en la transferencia y envía una notificación de creación a cada uno de ellos. Si se produce una excepción durante el
     * proceso, registra un mensaje de error y lanza una {@link NotificationSenderException}.
     */
    private void sendCreateNotification(Movement movement) {
        if (movement.getMovementType() == MovementType.TRANSFERENCIA){
            Account accountOfReference = movement.getAccountOfReference();
            Account accountOfDestination = movement.getAccountOfDestination();
            ClientResponseDto clientOfReference = clientService.findById(accountOfReference.getClient().getPublicId());
            ClientResponseDto clientOfDestination = clientService.findById(accountOfDestination.getClient().getPublicId());
            User firstUser = usersService.findById(clientOfReference.getUserResponse().getId());
            User secondUser = usersService.findById(clientOfDestination.getUserResponse().getId());

            try {
                Notification notification = Notification.builder()
                        .recipient(firstUser)
                        .message("Se ha realizado una transferencia en su cuenta bancaria")
                        .type(NotificationType.CREATE)
                        .build();
                sendNotificationToClient(notification, firstUser);
                Notification notification2 = Notification.builder()
                        .recipient(secondUser)
                        .message("Se ha recibido una transferencia a su cuenta bancaria")
                        .type(NotificationType.CREATE)
                        .build();
                sendNotificationToClient(notification2, secondUser);
            }catch (Exception e) {
                log.error("Error al enviar notificación: {}", e.getMessage());
                throw new NotificationSenderException("Error al enviar notificación: " + e);
            }
        }else {
            Account accountOfReference = movement.getAccountOfReference();
            ClientResponseDto clientOfReference = clientService.findById(accountOfReference.getClient().getPublicId());
            User firstUser = usersService.findById(clientOfReference.getUserResponse().getId());

            try {
                Notification notification = Notification.builder()
                        .recipient(firstUser)
                        .message("Se ha realizado un movimiento en su cuenta bancaria")
                        .type(NotificationType.CREATE)
                        .build();
                sendNotificationToClient(notification, firstUser);
            }catch (Exception e) {
                log.error("Error al enviar notificación: {}", e.getMessage());
                throw new NotificationSenderException("Error al enviar notificación: " + e);
            }
        }
    }

    /**
     * Envía una notificación basada en la cuenta y el tipo de notificación proporcionados.
     *
     * @param account La cuenta que desencadenó la notificación.
     * @param notificationType El tipo de notificación que se enviará.
     *
     * Esta función recupera el cliente y el usuario asociados con la cuenta proporcionada,
     * construye una notificación y la envía al cliente utilizando el WebSocketHandler.
     * Si se produce una excepción durante el proceso, se registra un mensaje de error y se lanza
     * una excepción NotificationSenderException.
     */
    @Override
    public void sendNotificationFromAccount(Account account, NotificationType notificationType) {
        log.info("Enviando una notificación");
        ClientResponseDto client = clientService.findById(account.getClient().getPublicId());
        User user = usersService.findById(client.getUserResponse().getId());

        try {
            Notification notification = Notification.builder()
                   .recipient(user)
                   .message("Se ha realizado un movimiento en su cuenta bancaria")
                   .type(NotificationType.CREATE)
                   .build();
            sendNotificationToClient(notification, user);
        }catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage());
            throw new NotificationSenderException("Error al enviar notificación: " + e);
        }
    }

    /**
     * Envia una notificación relacionada con la tarjeta de crédito proporcionada y el tipo de notificación.
     *
     * @param card La tarjeta de crédito que desencadenó la notificación. No puede ser nula.
     * @param notificationType El tipo de notificación que se enviará. No puede ser nulo.
     *
     * Este método verifica el tipo de notificación y llama al método de notificación correspondiente
     * (crear, actualizar o eliminar) según sea necesario.
     *
     * @throws NotificationSenderException Si se produce alguna excepción durante el proceso de envío de notificación.
     */
    @Override
    public void sendNotificationFromCard(Card card, NotificationType notificationType) {
        log.info("Enviando una notificación");
        if (notificationType == NotificationType.CREATE)
            sendCreateNotification(card);
        else if (notificationType == NotificationType.UPDATE)
            sendUpdateNotification(card);
        else if (notificationType == NotificationType.DELETE)
            sendDeleteNotification(card);
    }
    
    /**
     * Envia una notificación de actualización relacionada con la tarjeta de crédito proporcionada.
     *
     * @param card La tarjeta de crédito que desencadenó la notificación. No puede ser nula.
     *
     * Este método recupera el cliente y el usuario asociados con la cuenta de la tarjeta proporcionada,
     * construye una notificación de actualización y la envía al cliente utilizando el WebSocketHandler.
     * Si se produce una excepción durante el proceso, se registra un mensaje de error y se lanza
     * una excepción NotificationSenderException.
     */
    private void sendUpdateNotification(Card card) {
        ClientResponseDto client = clientService.findById(card.getAccount().getPublicId());
        User user = usersService.findById(client.getUserResponse().getId());

        try {
            Notification notification = Notification.builder()
                   .recipient(user)
                   .message("Se ha actualizado su tarjeta de crédito")
                   .type(NotificationType.UPDATE)
                   .build();
            sendNotificationToClient(notification, user);
        }catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage());
            throw new NotificationSenderException("Error al enviar notificación: " + e);
        }
    }
    
    /**
     * Envía una notificación de eliminación relacionada con la tarjeta de crédito proporcionada.
     *
     * @param card La tarjeta de crédito que desencadenó la notificación. No puede ser nula.
     *
     * Este método recupera el cliente y el usuario asociados con la cuenta de la tarjeta proporcionada,
     * construye una notificación de eliminación y la envía al cliente utilizando el WebSocketHandler.
     * Si se produce una excepción durante el proceso, se registra un mensaje de error y se lanza
     * una excepción NotificationSenderException.
     */
    private void sendDeleteNotification(Card card) {    
        ClientResponseDto client = clientService.findById(card.getAccount().getPublicId());
        User user = usersService.findById(client.getUserResponse().getId());

        try {
            Notification notification = Notification.builder()
                   .recipient(user)
                   .message("Se ha eliminado su tarjeta de crédito")
                   .type(NotificationType.DELETE)
                   .build();
            sendNotificationToClient(notification, user);
        }catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage());
            throw new NotificationSenderException("Error al enviar notificación: " + e);
        }
    }
    
    
    /**
     * Envía una notificación relacionada con la tarjeta de crédito recién creada.
     *
     * @param card La tarjeta de crédito que desencadenó la notificación. No puede ser nula.
     *
     * Este método recupera el cliente y el usuario asociados con la cuenta de la tarjeta proporcionada,
     * construye una notificación de creación y la envía al cliente utilizando el WebSocketHandler.
     * Si se produce una excepción durante el proceso, se registra un mensaje de error y se lanza
     * una excepción NotificationSenderException.
     *
     * @throws NotificationSenderException Si se produce una excepción durante el envío de la notificación.
     */
    private void sendCreateNotification(Card card) {
        ClientResponseDto client = clientService.findById(card.getAccount().getPublicId());
        User user = usersService.findById(client.getUserResponse().getId());

        try {
            Notification notification = Notification.builder()
                   .recipient(user)
                   .message("Se ha creado una nueva tarjeta de crédito")
                   .type(NotificationType.CREATE)
                   .build();
            sendNotificationToClient(notification, user);
        }catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage());
            throw new NotificationSenderException("Error al enviar notificación: " + e);
        }
    }

    /**
     * Envía una notificación relacionada con el cliente proporcionado y el tipo de notificación.
     *
     * @param client El cliente que desencadenó la notificación. No puede ser nulo.
     * @param notificationType El tipo de notificación que se enviará. No puede ser nulo.
     *
     * Este método verifica el tipo de notificación y llama al método de notificación correspondiente
     * (crear, actualizar o eliminar) según sea necesario.
     *
     * @throws NotificationSenderException Si se produce alguna excepción durante el proceso de envío de notificación.
     */
    @Override
    public void sendNotificationFromClient(Client client, NotificationType notificationType) {
        log.info("Enviando una notificación");
        if (notificationType == NotificationType.CREATE)
            sendCreateNotification(client);
        else if (notificationType == NotificationType.UPDATE)
            sendUpdateNotification(client);
        else if (notificationType == NotificationType.DELETE)
            sendDeleteNotification(client);
    }
    
    /**
     * Envía una notificación de actualización al cliente asociado con el cliente proporcionado.
     *
     * @param cliente El cliente que desencadenó la notificación. No puede ser nulo.
     *
     * Este método recupera al usuario asociado con el cliente, construye una notificación de actualización,
     * y la envía al cliente utilizando el WebSocketHandler. Si se produce una excepción durante el proceso,
     * se registra un mensaje de error y se lanza una excepción NotificationSenderException.
     *
     * @throws NotificationSenderException Si se produce una excepción durante el envío de la notificación.
     */
    private void sendUpdateNotification(Client cliente) {
        User usuario = usersService.findById(cliente.getUser().getPublicId());

        try {
            Notification notificacion = Notification.builder()
                   .recipient(usuario)
                   .message("Se ha actualizado su información personal")
                   .type(NotificationType.UPDATE)
                   .build();
            sendNotificationToClient(notificacion, usuario);
        }catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage());
            throw new NotificationSenderException("Error al enviar notificación: " + e);
        }
    }
    
    /**
     * Envía una notificación de eliminación al cliente asociado con el cliente proporcionado.
     *
     * @param cliente El cliente que desencadenó la notificación. No puede ser nulo.
     *
     * Este método recupera al usuario asociado, construye una notificación de eliminación,
     * y la envía al cliente utilizando el WebSocketHandler. Si se produce una excepción durante el proceso,
     * se registra un mensaje de error y se lanza una excepción NotificationSenderException.
     *
     * @throws NotificationSenderException Si se produce una excepción durante el envío de la notificación.
     */
    private void sendDeleteNotification(Client cliente) {
        User usuario = usersService.findById(cliente.getUser().getPublicId());

        try {
            Notification notificacion = Notification.builder()
                   .recipient(usuario)
                   .message("Se ha eliminado su cuenta")
                   .type(NotificationType.DELETE)
                   .build();
            sendNotificationToClient(notificacion, usuario);
        }catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage());
            throw new NotificationSenderException("Error al enviar notificación: " + e);
        }
    }
    
    /**
     * Envía una notificación al cliente asociado con el cliente proporcionado.
     *
     * Este método recupera al usuario asociado con el cliente, construye una nueva notificación,
     * y la envía al cliente utilizando el WebSocketHandler. Si se produce una excepción durante el proceso,
     * se registra un mensaje de error y se lanza una excepción NotificationSenderException.
     *
     * @param cliente El cliente que desencadenó la notificación. No puede ser nulo.
     * @throws NotificationSenderException Si se produce una excepción durante el envío de la notificación.
     */
    private void sendCreateNotification(Client cliente) {
        User usuario = usersService.findById(cliente.getUser().getPublicId());

        try {
            Notification notificacion = Notification.builder()
                   .recipient(usuario)
                   .message("Se ha creado una nueva cuenta")
                   .type(NotificationType.CREATE)
                   .build();
            sendNotificationToClient(notificacion, usuario);
        }catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage());
            throw new NotificationSenderException("Error al enviar notificación: " + e);
        }
    }

    /**
     * Envía una notificación al cliente asociado con el usuario proporcionado.
     *
     * Este método recupera al usuario asociado con el usuario proporcionado, construye una nueva notificación,
     * y la envía al cliente utilizando el WebSocketHandler. Si se produce una excepción durante el proceso,
     * se registra un mensaje de error y se lanza una excepción NotificationSenderException.
     *
     * @param notification La notificación que se enviará. No puede ser nula.
     * @param user El usuario al que se enviará la notificación. No puede ser nulo.
     * @throws IOException Si se produce un error al enviar la notificación.
     */
    private void sendNotificationToClient(Notification notification, User user) throws IOException {
        webSocketHandler.sendMessage(notification.toString(), user);
    }
}
