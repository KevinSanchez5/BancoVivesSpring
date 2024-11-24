package vives.bancovives.rest.users.services;

import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.rest.users.exceptions.UserConflict;
import vives.bancovives.rest.users.exceptions.UserNotFound;
import vives.bancovives.rest.users.mappers.UsersMapper;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.repositories.UsersRepository;
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

@Service
@Slf4j
@CacheConfig(cacheNames = {"users"})
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;

    public UsersServiceImpl(UsersRepository usersRepository, UsersMapper usersMapper) {
        this.usersRepository = usersRepository;
        this.usersMapper = usersMapper;
    }

    @Override
    public Page<UserResponse> findAll(Optional<String> username, Optional<Boolean> isDeleted, Pageable pageable) {
        log.info("Buscando todos los usuarios con username: " + username + " y borrados: " + isDeleted);
        // Criterio de búsqueda por nombre
        Specification<User> specUsernameUser = (root, query, criteriaBuilder) ->
                username.map(m -> criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + m.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));


        // Criterio de búsqueda por borrado
        Specification<User> specIsDeleted = (root, query, criteriaBuilder) ->
                isDeleted.map(m -> criteriaBuilder.equal(root.get("isDeleted"), m))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Combinamos las especificaciones
        Specification<User> criterio = Specification.where(specUsernameUser)
                .and(specIsDeleted);

        // Debe devolver un Page, por eso usamos el findAll de JPA
        return usersRepository.findAll(criterio, pageable).map(usersMapper::fromEntityToResponseDto);
    }

    @Override
    @Cacheable(key = "#publicId")
    public UserResponse findById(String publicId) {
        log.info("Buscando usuario por id: " + publicId);
        var user = usersRepository.findByPublicId(publicId).orElseThrow(() -> new UserNotFound(publicId));
        return usersMapper.fromEntityToResponseDto(user);
    }

    @Override
    @CachePut(key = "#result.publicId")
    public UserResponse save(UserRequest userRequest) {
        log.info("Guardando usuario: " + userRequest);
        // No debe existir otro con el mismo username o email
        usersRepository.findByUsernameEqualsIgnoreCase(userRequest.getUsername())
                .ifPresent(u -> {
                    throw new UserConflict("Ya existe un usuario con ese username");
                });
        return usersMapper.fromEntityToResponseDto(usersRepository.save(usersMapper.fromUpdateDtotoUser(userRequest)));
    }

    @Override
    @CachePut(key = "#result.publicId")
    public UserResponse update(String publicId, UserRequest userRequest) {
        log.info("Actualizando usuario: " + userRequest);
        User oldUser = usersRepository.findByPublicId(publicId).orElseThrow(() -> new UserNotFound(publicId));
        // No debe existir otro con el mismo username o email, y si existe soy yo mismo
        usersRepository.findByUsernameEqualsIgnoreCase(userRequest.getUsername())
                .ifPresent(u -> {
                    if (!u.getPublicId().equals(publicId)) {
                        throw new UserConflict("Ya existe un usuario con ese username");
                    }
                });
        User updatedUser = usersMapper.fromUpdateDtotoUser(oldUser, userRequest);
        return usersMapper.fromEntityToResponseDto(usersRepository.save(updatedUser));
    }

    @Override
    @Transactional
    @CacheEvict(key = "#publicId")
    public void deleteById(String publicId) {
        log.info("Borrando usuario por id: " + publicId);
        usersRepository.findByPublicId(publicId).orElseThrow(() -> new UserNotFound(publicId));
        usersRepository.updateIsDeletedToTrueByPublicId(publicId);
    }

}
