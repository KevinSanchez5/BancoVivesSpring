package vives.bancovives.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Clase dedicada a la generación y validación de tokens JWT
 *
 * @author Diego Novillo Luceño
 * @since 1.0.0
 */
@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.key.private}")
    private String privateKeyPath;

    @Value("${jwt.key.public}")
    private String publicKeyPath;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Extrae el nombre de usuario del token.
     * Utiliza el algoritmo ECDSA (Elliptic Curve Digital Signature Algorithm) con curva 256 bits (ES256).
     *
     * @param token Token JWT a analizar.
     * @return Nombre de usuario extraído del token.
     */
    @Override
    public String extractUserName(String token) {
        log.info("Extrayendo nombre de usuario del token: {}", token);
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getSubject();
    }

    /**
     * Genera un token JWT para un usuario determinado.
     * Utiliza el algoritmo ECDSA (Elliptic Curve Digital Signature Algorithm) con curva 256 bits (ES256).
     *
     * @param userDetails Detalles del usuario para el que se generará el token.
     * @return Token JWT generado.
     * @throws RuntimeException Si se produce algún error durante la generación del token.
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        log.info("Generando token para el usuario: {}", userDetails.getUsername());
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Valida un token JWT para un usuario determinado.
     * Utiliza el algoritmo ECDSA (Elliptic Curve Digital Signature Algorithm) con curva 256 bits (ES256).
     *
     * @param token Token JWT a validar.
     * @param userDetails Detalles del usuario para el que se validará el token.
     * @return true si el token es válido y pertenece al usuario especificado; de lo contrario, false.
     */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.info("Validando token: {} para el usuario: {}", token, userDetails.getUsername());
        if (!verifyToken(token)) return false;
        final String userName = extractUserName(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Genera un token JWT con datos extra.
     * Utiliza el algoritmo ECDSA (Elliptic Curve Digital Signature Algorithm) con curva 256 bits (ES256).
     *
     * @param extraClaims Datos extra a incluir en el token.
     * @param userDetails Detalles del usuario para el que se generará el token.
     * @return Token JWT generado.
     * @throws RuntimeException Sí se produce algún error durante la generación del token.
     */
    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        try {
            PrivateKey privateKey = loadPrivateKey();

            Algorithm algorithm = Algorithm.ECDSA256(null, (ECPrivateKey) privateKey);

            Date now = new Date();
            Date expirationDate = new Date(now.getTime() + (jwtExpiration * 1000)); // Convertir a milisegundos

            return JWT.create()
                    .withHeader(createHeader())
                    .withSubject(userDetails.getUsername())
                    .withIssuedAt(now)
                    .withExpiresAt(expirationDate)
                    .withClaim("extraClaims", extraClaims)
                    .sign(algorithm);
        } catch (Exception e) {
            log.error("Error generando token: {}", e.getMessage());
            throw new RuntimeException("Error generando token", e);
        }
    }

    /**
     * Comprueba si un token ha expirado.
     *
     * @param token Token JWT a analizar.
     * @return true si el token ha expirado; de lo contrario, false.
     */
    private boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        return expirationDate.before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token.
     *
     * @param token Token JWT a analizar.
     * @return Fecha de expiración del token.
     */
    public Date extractExpiration(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getExpiresAt();
    }

    /**
     * Carga la clave privada utilizando el algoritmo ECDSA (Elliptic Curve Digital Signature Algorithm) con curva 256 bits (ES256).
     *
     * @return Clave privada cargada.
     * @throws Exception Si se produce algún error durante la carga de la clave privada.
     */
    private PrivateKey loadPrivateKey() throws Exception {
        String privateKeyPEM = loadKeyContent(privateKeyPath)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    /**
     * Carga la clave pública utilizando el algoritmo ECDSA (Elliptic Curve Digital Signature Algorithm) con curva 256 bits (ES256).
     *
     * @return Clave pública cargada.
     * @throws Exception Si se produce algún error durante la carga de la clave pública.
     */
    private PublicKey loadPublicKey() throws Exception {
        String publicKeyPEM = loadKeyContent(publicKeyPath)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
    }

    /**
     * Verifica un token JWT utilizando la clave pública.
     * Utiliza el algoritmo ECDSA (Elliptic Curve Digital Signature Algorithm) con curva 256 bits (ES256).
     *
     * @param token Token JWT a verificar.
     * @return true si el token es válido; de lo contrario, false.
     */
    public boolean verifyToken(String token) {
        try {
            PublicKey publicKey = loadPublicKey();
            JWTVerifier verifier = JWT.require(Algorithm.ECDSA256((ECPublicKey) publicKey, null))
                    .build();
            verifier.verify(token); // Verifica el token utilizando la clave pública
            return true;
        } catch (Exception e) {
            log.error("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lee el contenido de una clave desde un archivo o recurso de clase.
     *
     * @param keyPath Ruta al archivo o recurso de clase que contiene la clave.
     * @return Contenido de la clave.
     * @throws Exception Si se produce algún error durante la lectura del contenido de la clave.
     */
    private String loadKeyContent(String keyPath) throws Exception {
        if (keyPath.startsWith("classpath:")) {
            String resourcePath = keyPath.substring("classpath:".length());
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    throw new IllegalArgumentException("Archivo de clave no encontrado: " + keyPath);
                }
                return new String(is.readAllBytes());
            }
        } else {
            return Files.readString(Path.of(keyPath));
        }
    }

    /**
     * Crea el encabezado JWT con información básica.
     *
     * @return Mapa con la información del encabezado JWT.
     */
    private Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "ES256");
        return header;
    }
}