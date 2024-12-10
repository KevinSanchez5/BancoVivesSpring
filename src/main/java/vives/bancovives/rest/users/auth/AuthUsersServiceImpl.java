package vives.bancovives.rest.users.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.users.repositories.UsersRepository;

@Service("userDetailsService")
public class AuthUsersServiceImpl implements AuthUsersService {

    private final UsersRepository authUsersRepository;

    @Autowired
    public AuthUsersServiceImpl(UsersRepository authUsersRepository) {
        this.authUsersRepository = authUsersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return authUsersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario con username " + username + " no encontrado"));
    }
}