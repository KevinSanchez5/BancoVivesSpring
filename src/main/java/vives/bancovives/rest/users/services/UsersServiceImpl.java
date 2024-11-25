package vives.bancovives.rest.users.services;

import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
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
import vives.bancovives.rest.users.validator.UserUpdateValidator;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@CacheConfig(cacheNames = {"users"})
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final UserUpdateValidator userUpdateValidator;

    public UsersServiceImpl(UsersRepository usersRepository, UsersMapper usersMapper, UserUpdateValidator userUpdateValidator) {
        this.usersRepository = usersRepository;
        this.usersMapper = usersMapper;
        this.userUpdateValidator = userUpdateValidator;
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
        User user = existsUserByPublicId(publicId);
        return usersMapper.fromEntityToResponseDto(user);
    }

    @Override
    @CachePut(key = "#result.publicId")
    public UserResponse save(UserRequest userRequest) {
        log.info("Guardando usuario: " + userRequest);
        existsUserByUsername(userRequest.getUsername());
        return usersMapper.fromEntityToResponseDto(usersRepository.save(usersMapper.fromUpdateDtotoUser(userRequest)));
    }

    @Override
    @CachePut(key = "#result.publicId")
    public UserResponse update(String publicId, UserUpdateDto updateDto) {
        log.info("Actualizando usuario: " + updateDto);
        User oldUser = existsUserByPublicId(publicId);
        userUpdateValidator.validateUpdate(updateDto);
        existsUserByUsername(updateDto.getUsername());
        User updatedUser = usersMapper.fromUpdateDtotoUser(oldUser, updateDto);
        return usersMapper.fromEntityToResponseDto(usersRepository.save(updatedUser));
    }

    @Override
    @Transactional
    @CacheEvict(key = "#publicId")
    public void deleteById(String publicId) {
        log.info("Borrando usuario por id: " + publicId);
        User userToDelete = existsUserByPublicId(publicId);
        usersRepository.deleteById(userToDelete.getId());
    }

    public void existsUserByUsername(String username) {
        if (usersRepository.findByUsernameEqualsIgnoreCase(username).isPresent()) {
            throw new UserConflict("Ya existe un usuario con ese username");
        }
    }

    public User existsUserByPublicId(String publicId) {
        return usersRepository.findByPublicId(publicId).orElseThrow(() -> new UserNotFound(publicId));
    }

    public User saveUserFromClient(User user) {
        log.info("Guardando usuario desde cliente");
        existsUserByUsername(user.getUsername());
        return usersRepository.save(user);
    }

    public User updateUserFromClient(String publicId, User updateUser) {
        log.info("Actualizando usuario desde cliente");
        User oldUser = existsUserByPublicId(publicId);
        if(updateUser.getUpdatedAt()!=null){
            existsUserByUsername(updateUser.getUsername());
        }
        User updatedUser = usersMapper.updateUserFromClient(oldUser, updateUser);
        return usersRepository.save(updatedUser);
    }
}
