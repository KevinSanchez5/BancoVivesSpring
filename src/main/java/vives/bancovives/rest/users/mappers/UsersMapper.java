package vives.bancovives.rest.users.mappers;

import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.rest.users.models.User;
import org.springframework.stereotype.Component;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class UsersMapper {
    public User fromUpdateDtotoUser(UserRequest request) {
        return User.builder()
                .id(UUID.randomUUID())
                .publicId(IdGenerator.generateId())
                .username(request.getUsername())
                .password(request.getPassword())
                .roles(request.getRoles())
                .isDeleted(request.getIsDeleted())
                .build();
    }

    public User fromUpdateDtotoUser(User oldUser, UserRequest request) {
        return new User(
                oldUser.getId(),
                oldUser.getPublicId(),
                request.getUsername() !=null ? request.getUsername() : oldUser.getUsername(),
                request.getPassword() !=null ? request.getPassword() : oldUser.getPassword(),
                request.getRoles() !=null ? request.getRoles() : oldUser.getRoles(),
                oldUser.getCreatedAt(),
                LocalDateTime.now(),
                request.getIsDeleted() !=null ? request.getIsDeleted() : oldUser.getIsDeleted()
                );
    }

    public UserResponse fromEntityToResponseDto(User user) {
        return UserResponse.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .isDeleted(user.getIsDeleted())
                .build();
    }

}
