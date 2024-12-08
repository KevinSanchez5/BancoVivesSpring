package vives.bancovives.rest.users.mappers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UsersMapperTest {

    private final UsersMapper usersMapper = new UsersMapper();

    @Test
    void fromRequestDtotoUser_shouldMapUserRequestToUser() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("testUser");
        request.setPassword("password123");
        request.setRoles(Set.of(Role.USER));

        // Act
        User result = usersMapper.fromRequestDtotoUser(request);

        // Assert
        assertEquals(request.getUsername(), result.getUsername());
        assertEquals(request.getPassword(), result.getPassword());
        assertEquals(request.getRoles(), result.getRoles());
    }

    @Test
    void fromUpdateDtotoUser_shouldUpdateUserFields() {
        // Arrange
        User oldUser = User.builder()
                .username("oldUser")
                .password("oldPassword")
                .roles(Set.of(Role.USER))
                .build();
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("newUser");
        updateDto.setPassword("newPassword");
        updateDto.setRoles(Set.of(Role.ADMIN));

        // Act
        User result = usersMapper.fromUpdateDtotoUser(oldUser, updateDto);

        // Assert
        assertEquals(oldUser.getId(), result.getId());
        assertEquals(oldUser.getPublicId(), result.getPublicId());
        assertEquals("newUser", result.getUsername());
        assertEquals("newPassword", result.getPassword());
        assertEquals(Set.of(Role.ADMIN), result.getRoles());
    }

    @Test
    void fromEntityToResponseDto_shouldMapUserToUserResponse() {
        // Arrange
        User user = User.builder()
                .publicId("publicId123")
                .username("testUser")
                .roles(Set.of(Role.USER))
                .isDeleted(false)
                .build();

        // Act
        UserResponse result = usersMapper.fromEntityToResponseDto(user);

        // Assert
        assertEquals(user.getPublicId(), result.getPublicId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getRoles(), result.getRoles());
        assertEquals(user.getIsDeleted(), result.getIsDeleted());
    }

    @Test
    void updateUserFromClient_shouldUpdateUserFieldsFromNewUser() {
        // Arrange
        User oldUser = User.builder()
                .username("oldUser")
                .password("oldPassword")
                .roles(Set.of(Role.USER))
                .build();
        User newUser = User.builder()
                .username("newUser")
                .password("newPassword")
                .roles(Set.of(Role.ADMIN))
                .build();

        // Act
        User result = usersMapper.updateUserFromClient(oldUser, newUser);

        // Assert
        assertEquals(oldUser.getId(), result.getId());
        assertEquals(oldUser.getPublicId(), result.getPublicId());
        assertEquals("newUser", result.getUsername());
        assertEquals("newPassword", result.getPassword());
        assertEquals(Set.of(Role.ADMIN), result.getRoles());
    }
}

