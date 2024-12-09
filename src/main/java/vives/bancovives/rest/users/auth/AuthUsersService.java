package vives.bancovives.rest.users.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthUsersService extends UserDetailsService {
    @Override
    UserDetails loadUserByUsername(String username);
}