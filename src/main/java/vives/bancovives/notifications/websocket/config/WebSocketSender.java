package vives.bancovives.notifications.websocket.config;

import vives.bancovives.rest.users.models.User;

import java.io.IOException;

/**
 * Interfaz para enviar mensajes por WebSockets
 */
public interface WebSocketSender {

    void sendMessage(String message, User user) throws IOException;

}