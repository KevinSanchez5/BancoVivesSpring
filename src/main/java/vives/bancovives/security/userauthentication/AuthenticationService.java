package vives.bancovives.security.userauthentication;

import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.security.model.JwtAuthResponse;

public interface AuthenticationService {
    JwtAuthResponse signUp(UserRequest request);

    JwtAuthResponse signIn(UserRequest request);
}