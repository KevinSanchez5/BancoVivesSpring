package vives.bancovives.rest.users.validator;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.exceptions.UserBadRequest;

@Component
public class UserUpdateValidator {

    public void validateUpdate(UserUpdateDto dto) {
        if (dto.getUsername() == null  && dto.getPassword() == null && dto.getRoles() == null) {
            throw new UserBadRequest("Username y password no pueden estar vacíos");
        }
        if(dto.getPassword()!= null && dto.getPassword().length() < 5 && dto.getPassword().isBlank()){
            throw new UserBadRequest("La contraseña debe tener al menos 5 caracteres");
        }
        if(dto.getUsername()!= null && dto.getUsername().trim().isBlank()){
            throw new UserBadRequest("El nombre de usuario no puede estar en blanco");
        }
    }
}
