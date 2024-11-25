package vives.bancovives.rest.users.validator;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.exceptions.UserBadRequest;

@Component
public class UserUpdateValidator {

    public void validateUpdate(UserUpdateDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isEmpty() && dto.getPassword() == null || dto.getPassword().isEmpty()
                && dto.getRoles() == null || dto.getRoles().isEmpty()) {
            throw new UserBadRequest("Username y password no pueden estar vacíos");
        }
    }
}
