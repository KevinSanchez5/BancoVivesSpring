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
    public User fromUpdateDtotoUser(UserRequest request) {
        return User.builder()
                .id(UUID.randomUUID())
                .publicId(IdGenerator.generateId())
                .username(request.getUsername())
                .password(request.getPassword())
                .roles(request.getRoles())
                .build();
    }

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

    public UserResponse fromEntityToResponseDto(User user) {
        return UserResponse.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .isDeleted(user.getIsDeleted())
                .build();
    }

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
