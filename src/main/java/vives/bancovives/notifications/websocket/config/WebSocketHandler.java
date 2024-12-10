package vives.bancovives.notifications.websocket.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.security.jwt.JwtService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class WebSocketHandler extends TextWebSocketHandler implements SubProtocolCapable, WebSocketSender{
    private final String entity;
    private final JwtService jwtService;
    private UsersService userService;

    // Sesiones de los clientes conectados, para recorrelos y enviarles mensajes (patrón observer)
    // es concurrente porque puede ser compartida por varios hilos
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    public WebSocketHandler(
            String entity, JwtService jwtService, UsersService usersService) {
        this.entity = entity;
        this.jwtService = jwtService;
        this.userService = usersService;
    }

    /**
     * Cuando se establece la conexión con el servidor
     *
     * @param session Sesión del cliente
     * @throws Exception Error al establecer la conexión
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Conexión establecida con el servidor");
        Map<String, Object> attributes = session.getAttributes();
        String token = (String) attributes.get("token");
        if (token != null) {
            DecodedJWT decodedJWT = JWT.decode(token);
            String username = decodedJWT.getSubject();
            User user = userService.findUserByUsername(username);
            if (jwtService.isTokenValid(token,user)){
                log.info("Sesión: {} - Usuario: {}", session, username);
                attributes.put("username", username);
                sessions.add(session);
                TextMessage message = new TextMessage("Se ha conectado al websocket con el usuario: " + username);
                log.info("Servidor envía: {}", message);
                session.sendMessage(message);
            }else {
                log.warn("Token inválido, cerrando sesión: {}", session);
                session.close(CloseStatus.NOT_ACCEPTABLE);
            }
        }else {
            log.warn("Token inválido, cerrando sesión: {}", session);
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }

    }

    /**
     * Cuando se cierra la conexión con el servidor
     *
     * @param session Sesión del cliente
     * @param status  Estado de la conexión
     * @throws Exception Error al cerrar la conexión
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Conexión cerrada con el servidor: " + status);
        sessions.remove(session);
    }

    /**
     * Envía un mensaje a todos los clientes conectados
     *
     * @param message Mensaje a enviar
     * @throws IOException Error al enviar el mensaje
     */
    @Override
    public void sendMessage(String message, User user) throws IOException {
        log.info("Enviar mensaje de cambios en la entidad: " + entity + " : " + message);
        // Enviamos el mensaje a todos los clientes conectados
        for (WebSocketSession session : sessions) {
            if (session.isOpen() && session.getAttributes().get("username").equals(user.getUsername())) {
                log.info("Servidor WS envía: " + message);
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    /**
     * Maneja los errores de transporte que le llegan al servidor
     *
     * @param session   Sesión del cliente
     * @param exception Excepción que se ha producido
     * @throws Exception Error al manejar el error
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("Error de transporte con el servidor: " + exception.getMessage());
    }

    /**
     * Devuelve los subprotocolos que soporta el servidor
     *
     * @return Lista de subprotocolos
     */
    @Override
    public List<String> getSubProtocols() {
        return List.of("subprotocol.demo.websocket");
    }

}