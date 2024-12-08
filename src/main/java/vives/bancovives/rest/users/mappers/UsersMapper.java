package vives.bancovives.rest.users.mappers;

import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.rest.users.models.User;
import org.springframework.stereotype.Component;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class UsersMapper {
    /**
     * Mapea un objeto {@link UserRequest} a un objeto {@link User}.
     *
     * @param request El objeto {@link UserRequest} que contiene la información del usuario.
     * @return Un objeto {@link User} con la información proporcionada.
     */
    public User fromRequestDtotoUser(UserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .roles(request.getRoles())
                .build();
        return user;
    }

    /**
     * Actualiza un objeto User con la información de un objeto UserUpdateDto.
     *
     * @param oldUser El objeto {@link User} original que se va a actualizar.
     * @param updateDto El objeto {@link UserUpdateDto} que contiene la información actualizada.
     * @return Un objeto {@link User} actualizado con la nueva información.
     */
    public User fromUpdateDtotoUser(User oldUser, UserUpdateDto updateDto) {
        return new User(
                oldUser.getId(),
                oldUser.getPublicId(),
                updateDto.getUsername() !=null ? updateDto.getUsername() : oldUser.getUsername(),
                updateDto.getPassword() !=null ? updateDto.getPassword() : oldUser.getPassword(),
                updateDto.getRoles() !=null ? updateDto.getRoles() : oldUser.getRoles(),
                oldUser.getClient(),
                oldUser.getCreatedAt(),
                LocalDateTime.now(),
                oldUser.getIsDeleted()
                );
    }

    /**
     * Mapea un objeto {@link User} a un objeto UserResponse.
     *
     * @param user El objeto {@link User} que se va a mapear.
     * @return Un objeto {@link UserUpdateDto} con la información del usuario.
     */
    public UserResponse fromEntityToResponseDto(User user) {
        return UserResponse.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .isDeleted(user.getIsDeleted())
                .build();
    }

    /**
     * Actualiza un objeto User con la información de otro objeto {@link User} (desde un cliente).
     *
     * @param oldUser El objeto {@link User} original que se va a actualizar.
     * @param newUser El objeto {@link User} que contiene la información actualizada (desde un cliente).
     * @return Un objeto {@link User} actualizado con la nueva información.
     */
    public User updateUserFromClient(User oldUser, User newUser){
        return new User(
                oldUser.getId(),
                oldUser.getPublicId(),
                newUser.getUsername() !=null ? newUser.getUsername() : oldUser.getUsername(),
                newUser.getPassword() !=null ? newUser.getPassword() : oldUser.getPassword(),
                newUser.getRoles() !=null ? newUser.getRoles() : oldUser.getRoles(),
                oldUser.getClient(),
                oldUser.getCreatedAt(),
                LocalDateTime.now(),
                oldUser.getIsDeleted()
        );
    }
}
