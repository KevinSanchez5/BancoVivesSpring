package vives.bancovives.rest.users.services;

import vives.bancovives.rest.users.dto.UserInfoResponse;
import vives.bancovives.rest.users.dto.UserRequest;
import vives.bancovives.rest.users.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UsersService {

    Page<UserResponse> findAll(Optional<String> username, Optional<String> email, Optional<Boolean> isDeleted, Pageable pageable);

    UserInfoResponse findById(UUID id);

    UserResponse save(UserRequest userRequest);

    UserResponse update(UUID id, UserRequest userRequest);

    void deleteById(UUID id);

}
