package vives.bancovives.rest.users.services;

import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.dto.output.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.users.models.User;

import java.util.Optional;

public interface UsersService {

    Page<User> findAll(Optional<String> username, Optional<Boolean> isDeleted, Pageable pageable);

    User findById(String id);

    User findUserByUsername(String username);

    User save(UserRequest userRequest);

    User update(String id, UserUpdateDto updateDto);

    void deleteById(String id);

    User saveUserFromClient(User user);

    User updateUserFromClient(String publicId, User user);
}
