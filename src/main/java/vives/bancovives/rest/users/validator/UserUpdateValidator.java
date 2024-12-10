package vives.bancovives.rest.users.validator;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.exceptions.UserValidationException;
import vives.bancovives.rest.users.models.Role;

import java.util.Set;

/**
 * Esta clase se encarga de validar las solicitudes de actualización de usuarios.
 * Contiene métodos para validar el nombre de usuario, la contraseña y los roles.
 *
 * @author Kevin Sánchez
 * @since 1.0
 */
@Component
public class UserUpdateValidator {

    /**
     * Valida la solicitud de actualización de usuario.
     *
     * @param dto El objeto de transferencia de datos (DTO) de actualización de usuario.
     * @throws UserValidationException Si alguna validación falla.
     */
    public void validateUpdate(UserUpdateDto dto) {
        validateEmptyAttributes(dto.getUsername(), dto.getPassword(), dto.getRoles());
        validatePassword(dto.getPassword());
        validateUsername(dto.getUsername());
    }

    /**
     * Valida si el nombre de usuario, la contraseña y los roles no están vacíos.
     *
     * @param username El nombre de usuario para validar.
     * @param password La contraseña para validar.
     * @param roles Los roles para validar.
     * @throws UserValidationException Si algún atributo está vacío.
     */
    private void validateEmptyAttributes(String username, String password, Set<Role> roles){
        if(username==null && password==null && roles.isEmpty() || roles == null){
            throw new UserValidationException("Los campos no pueden estar vacíos");
        }
    }

    /**
     * Valida la longitud de la contraseña tomando valores nulos como válidos.
     *
     * @param password La contraseña para validar.
     * @throws UserValidationException Si la contraseña tiene menos de 5 caracteres.
     */
    private void validatePassword(String password){
        if(password != null){
            if (password.length() < 5 || password.isBlank()){
                throw new UserValidationException("La contraseña debe tener al menos 5 caracteres");
            }
        }
    }

    /**
     * Valida la longitud del nombre de usuario tomando valores nulos como válidos.
     *
     * @param username El nombre de usuario para validar.
     * @throws UserValidationException Si el nombre de usuario tiene menos de 5 caracteres.
     */
    private void validateUsername(String username){
        if(username!=null && username.trim().isBlank()){
            throw new UserValidationException("El nombre de usuario debe tener al menos 5 caracteres");
        }
    }
}
