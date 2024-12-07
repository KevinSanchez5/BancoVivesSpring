package vives.bancovives.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.security.jwt.JwtService;

import java.io.IOException;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

/**
 * Esta clase es un filtro utilizado para la autenticación JWT (JSON Web Tokens) en una aplicación Spring Boot.
 * Hereda de {@link OncePerRequestFilter} y se encarga de validar y autenticar las solicitudes entrantes.
 *
 * @author Diego Novillo Luceño
 * @since 1.0.0
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * El servicio JWT utilizado para la generación y validación de tokens.
     */
    private final JwtService jwtService;

    /**
     * El servicio de usuarios utilizado para recuperar los detalles del usuario.
     */
    private final UsersService userService;

    /**
     * Constructor para la clase JwtAuthenticationFilter.
     *
     * @param jwtService El servicio JWT que se utilizará.
     * @param userService El servicio de usuarios que se utilizará.
     */
    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, UsersService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * Este método se llama para cada solicitud entrante y realiza el proceso de autenticación JWT.
     *
     * @param request La solicitud HTTP entrante.
     * @param response La respuesta HTTP saliente.
     * @param filterChain La cadena de filtros que se ejecutarán después de este filtro.
     * @throws ServletException Si se produce un error específico de servlet.
     * @throws IOException Si se produce un error de E/S.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        log.info("Iniciando el filtro de autenticación");
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        UserDetails userDetails = null;
        String userName = null;

        // Si no tenemos cabecera o no empieza por Bearer, no hacemos nada
        if (!StringUtils.hasText(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")) {
            log.info("No se ha encontrado cabecera de autenticación, se ignora");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Se ha encontrado cabecera de autenticación, se procesa");
        // Si tenemos cabecera, la extraemos y comprobamos que sea válida
        jwt = authHeader.substring(7);
        // Lo primero que debemos ver es que el token es válido
        try {
            userName = jwtService.extractUserName(jwt);
        } catch (Exception e) {
            log.info("Token no válido");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no autorizado o no válido");
            return;
        }
        log.info("Usuario autenticado: {}", userName);
        if (StringUtils.hasText(userName)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("Comprobando usuario y token");
            try {
                userDetails = userService.findUserByUsername(userName);
            } catch (Exception e) {
                log.info("Usuario no encontrado: {}", userName);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario no autorizado");
                return;
            }
            log.info("Usuario encontrado: {}", userDetails);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                log.info("JWT válido");
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "El token proporcionado no es válido");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
