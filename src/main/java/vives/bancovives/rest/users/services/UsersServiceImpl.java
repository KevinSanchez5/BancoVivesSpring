package vives.bancovives.rest.users.services;

import vives.bancovives.rest.users.dto.UserInfoResponse;
import vives.bancovives.rest.users.dto.UserRequest;
import vives.bancovives.rest.users.dto.UserResponse;
import vives.bancovives.rest.users.exceptions.UserNameOrEmailExists;
import vives.bancovives.rest.users.exceptions.UserNotFound;
import vives.bancovives.rest.users.mappers.UsersMapper;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.repositories.UsersRepository;
import vives.bancovives.rest.products.accounttype.repositories.AccountTypeRepository;
import vives.bancovives.rest.products.cardtype.repositories.CardTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@CacheConfig(cacheNames = {"users"})
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final CardTypeRepository cardTypeRepository;
    private final UsersMapper usersMapper;

    public UsersServiceImpl(UsersRepository usersRepository, AccountTypeRepository accountTypeRepository, CardTypeRepository cardTypeRepository, UsersMapper usersMapper) {
        this.usersRepository = usersRepository;
        this.accountTypeRepository = accountTypeRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.usersMapper = usersMapper;
    }

    @Override
    public Page<UserResponse> findAll(Optional<String> username, Optional<String> email, Optional<Boolean> isDeleted, Pageable pageable) {
        log.info("Buscando todos los usuarios con username: " + username + " y borrados: " + isDeleted);
        // Criterio de búsqueda por nombre
        Specification<User> specUsernameUser = (root, query, criteriaBuilder) ->
                username.map(m -> criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + m.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Criterio de búsqueda por email
        Specification<User> specEmailUser = (root, query, criteriaBuilder) ->
                email.map(m -> criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + m.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Criterio de búsqueda por borrado
        Specification<User> specIsDeleted = (root, query, criteriaBuilder) ->
                isDeleted.map(m -> criteriaBuilder.equal(root.get("isDeleted"), m))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Combinamos las especificaciones
        Specification<User> criterio = Specification.where(specUsernameUser)
                .and(specEmailUser)
                .and(specIsDeleted);

        // Debe devolver un Page, por eso usamos el findAll de JPA
        return usersRepository.findAll(criterio, pageable).map(usersMapper::toUserResponse);
    }

    @Override
    @Cacheable(key = "#id")
    public UserInfoResponse findById(UUID id) {
        log.info("Buscando usuario por id: " + id);
        var user = usersRepository.findById(id).orElseThrow(() -> new UserNotFound(id));
        var accounts = accountTypeRepository.findAccountIdsByUserId(id).stream().map(UUID::toString).toList();
        var cards = cardTypeRepository.findCardIdsByUserId(id).stream().map(UUID::toString).toList();
        return usersMapper.toUserInfoResponse(user, accounts, cards);
    }

    @Override
    @CachePut(key = "#result.id")
    public UserResponse save(UserRequest userRequest) {
        log.info("Guardando usuario: " + userRequest);
        // No debe existir otro con el mismo username o email
        usersRepository.findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase(userRequest.getUsername(), userRequest.getEmail())
                .ifPresent(u -> {
                    throw new UserNameOrEmailExists("Ya existe un usuario con ese username o email");
                });
        return usersMapper.toUserResponse(usersRepository.save(usersMapper.toUser(userRequest)));
    }

    @Override
    @CachePut(key = "#result.id")
    public UserResponse update(UUID id, UserRequest userRequest) {
        log.info("Actualizando usuario: " + userRequest);
        usersRepository.findById(id).orElseThrow(() -> new UserNotFound(id));
        // No debe existir otro con el mismo username o email, y si existe soy yo mismo
        usersRepository.findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase(userRequest.getUsername(), userRequest.getEmail())
                .ifPresent(u -> {
                    if (!u.getId().equals(id)) {
                        throw new UserNameOrEmailExists("Ya existe un usuario con ese username o email");
                    }
                });
        return usersMapper.toUserResponse(usersRepository.save(usersMapper.toUser(userRequest, id)));
    }

    @Override
    @Transactional
    @CacheEvict(key = "#id")
    public void deleteById(UUID id) {
        log.info("Borrando usuario por id: " + id);
        User user = usersRepository.findById(id).orElseThrow(() -> new UserNotFound(id));
        // Hacemos el borrado físico si no hay cuentas o tarjetas
        if (accountTypeRepository.existsByUserId(id) || cardTypeRepository.existsByUserId(id)) {
            log.info("Borrado lógico de usuario por id: " + id);
            usersRepository.updateIsDeletedToTrueById(id);
        } else {
            log.info("Borrado físico de usuario por id: " + id);
            usersRepository.delete(user);
        }
    }
}
