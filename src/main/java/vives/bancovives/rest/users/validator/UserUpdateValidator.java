package vives.bancovives.rest.users.validator;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.exceptions.UserBadRequest;
import vives.bancovives.rest.users.models.Role;

import java.util.List;
import java.util.Set;

@Component
public class UserUpdateValidator {

    public void validateUpdate(UserUpdateDto dto) {
        validateEmptyAttributes(dto.getUsername(), dto.getPassword(), dto.getRoles());
        validatePassword(dto.getPassword());
        validateUsername(dto.getUsername());
    }

    private void validateEmptyAttributes(String username, String password, Set<Role> roles){
        if(username==null && password==null && roles.isEmpty() || roles == null){
            throw new UserBadRequest("Los campos no pueden estar vacíos");
        }
    }

    private void validatePassword(String password){
        if(password!=null && password.length() < 5 && password.isBlank()){
            throw new UserBadRequest("La contraseña debe tener al menos 5 caracteres");
        }
    }

    private void validateUsername(String username){
        if(username!=null && username.trim().isBlank()){
            throw new UserBadRequest("El nombre de usuario debe tener al menos 5 caracteres");
        }
    }
}
