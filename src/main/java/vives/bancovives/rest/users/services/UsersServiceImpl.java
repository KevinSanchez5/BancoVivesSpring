package vives.bancovives.rest.users.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import vives.bancovives.rest.users.auth.AuthUsersService;
import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.exceptions.IncorrectPasswordException;
import vives.bancovives.rest.users.exceptions.UserAlreadyExistsException;
import vives.bancovives.rest.users.exceptions.UserNotFoundException;
import vives.bancovives.rest.users.mappers.UsersMapper;
import vives.bancovives.rest.users.models.Role;
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
import vives.bancovives.security.jwt.JwtService;
import vives.bancovives.security.model.JwtAuthResponse;

import java.util.Optional;
import java.util.Set;

/**
 * Implementación del servicio de usuarios.
 * Proporciona operaciones CRUD y funcionalidades de autenticación para los usuarios.
 */
@Service
@Slf4j
@CacheConfig(cacheNames = {"users"})
public class UsersServiceImpl implements UsersService {

    private final AuthUsersService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final UserUpdateValidator userUpdateValidator;

    public UsersServiceImpl(
            AuthUsersService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UsersRepository usersRepository,
            UsersMapper usersMapper,
            UserUpdateValidator userUpdateValidator
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.usersRepository = usersRepository;
        this.usersMapper = usersMapper;
        this.userUpdateValidator = userUpdateValidator;
    }

    /**
     * Busca todos los usuarios según los criterios de búsqueda y devuelve una página de resultados.
     *
     * @param username Nombre de usuario para filtrar (opcional)
     * @param isDeleted Indica si se deben mostrar los usuarios borrados (opcional)
     * @param pageable Parámetros de paginación
     * @return Página de resultados de usuarios
     */
    @Override
    public Page<User> findAll(Optional<String> username, Optional<Boolean> isDeleted, Pageable pageable) {
        log.info("Buscando todos los usuarios con username: " + username + " y borrados: " + isDeleted);
        // Criterio de búsqueda por nombre
        Specification<User> specUsernameUser = (root, query, criteriaBuilder) ->
                username.map(m -> criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + m.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Criterio de búsqueda por borrado
        Specification<User> specIsDeleted = (root, query, criteriaBuilder) ->
                isDeleted.map(m -> criteriaBuilder.equal(root.get("isDeleted"), m))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<User> criterio = Specification.where(specUsernameUser)
                .and(specIsDeleted);
        return usersRepository.findAll(criterio, pageable);
    }

    /**
     * Busca un usuario por su identificador público y lo devuelve.
     *
     * @param publicId Identificador público del usuario
     * @return Usuario encontrado
     */
    @Override
    @Cacheable(key = "#publicId")
    public User findById(String publicId) {
        log.info("Buscando usuario por id: " + publicId);
        return findByPublicId(publicId);
    }

    /**
     * Guarda un nuevo usuario en la base de datos y devuelve un token de autenticación.
     *
     * @param userRequest Datos del nuevo usuario
     * @return Token de autenticación
     */
    @Override
    @CachePut(key = "#result.publicId")
    public JwtAuthResponse save(UserRequest userRequest) {
        log.info("Guardando usuario: " + userRequest);
        validateUsernameIsNotTaken(userRequest.getUsername());
        userRequest.setRoles(Set.of(Role.ADMIN));
        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        verifyRoleDoesntIncludeSuperAdmin(userRequest);
        User user = usersMapper.fromRequestDtotoUser(userRequest);
        User userStored = usersRepository.save(user);
        return JwtAuthResponse.builder().token(jwtService.generateToken(userStored)).build();
    }

    /**
     * Actualiza los datos de un usuario existente en la base de datos.
     *
     * @param publicId Identificador público del usuario
     * @param updateDto Nuevos datos del usuario
     * @return Usuario actualizado
     */
    @Override
    @CachePut(key = "#result.publicId")
    public User update(String publicId, UserUpdateDto updateDto) {
        log.info("Actualizando usuario: " + updateDto);
        User oldUser = findByPublicId(publicId);
        userUpdateValidator.validateUpdate(updateDto);
        validateUsernameIsNotTaken(updateDto.getUsername());
        User updatedUser = usersMapper.fromUpdateDtotoUser(oldUser, updateDto);
        updatedUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        return usersRepository.save(updatedUser);
    }

    /**
     * Borra un usuario de la base de datos.
     *
     * @param publicId Identificador público del usuario
     */
    @Override
    @Transactional
    @CacheEvict(key = "#publicId")
    public void deleteById(String publicId) {
        log.info("Borrando administrador con id: " + publicId);
        User userToDelete = findByPublicId(publicId);
        usersRepository.deleteById(userToDelete.getId());
    }

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario del usuario
     * @return Usuario encontrado
     */
    @Override
    public User findUserByUsername(String username) {
        log.info("Buscando usuario por nombre de usuario: " + username);
        return usersRepository.findByUsernameEqualsIgnoreCase(username)
                .orElseThrow(
                        () -> new UserNotFoundException("El usuario con el nombre de usuario: " + username + " no existe")
                );
    }

    /**
     * Valida si un usuario con el nombre de usuario dado ya existe en la base de datos.
     * Si el usuario existe, lanza una excepción UserAlreadyExistsException.
     *
     * @param username El nombre de usuario a comprobar.
     * @throws UserAlreadyExistsException Si un usuario con el nombre de usuario dado ya existe.
     */
    public void validateUsernameIsNotTaken(String username) {
        log.info("Buscando usuario con el nombre de usuario: " + username);
        Optional<User> user = usersRepository.findByUsernameEqualsIgnoreCase(username);
        if (user.isPresent()) {
            throw new UserAlreadyExistsException("Ya existe un usuario con ese nombre de usuario");
        }
    }

    /**
     * Busca un usuario por su identificador público.
     * Si el usuario no se encuentra, lanza una excepción UserNotFoundException.
     *
     * @param publicId El identificador público del usuario a buscar.
     * @return El usuario encontrado.
     * @throws UserNotFoundException Si el usuario no se encuentra.
     */
    public User findByPublicId(String publicId) {
        return usersRepository.findByPublicId(publicId).orElseThrow(() -> new UserNotFoundException("El usuario con id: " + publicId + " no existe"));
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
     * Antes de guardar, comprueba si un usuario con el mismo nombre de usuario ya existe.
     *
     * @param user El usuario a guardar.
     * @return El usuario guardado.
     */
    public User saveUserFromClient(User user) {
        log.info("Guardando usuario desde cliente");
        validateUsernameIsNotTaken(user.getUsername());
        return usersRepository.save(user);
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     * Antes de actualizar, comprueba si un usuario con el mismo nombre de usuario ya existe.
     *
     * @param publicId El identificador público del usuario a actualizar.
     * @param updateUser Los nuevos datos del usuario.
     * @return El usuario actualizado.
     */
    public User updateUserFromClient(String publicId, User updateUser) {
        log.info("Actualizando usuario desde cliente");
        User oldUser = findByPublicId(publicId);
        if(updateUser.getUpdatedAt()!=null){
            validateUsernameIsNotTaken(updateUser.getUsername());
        }
        User updatedUser = usersMapper.updateUserFromClient(oldUser, updateUser);
        return usersRepository.save(updatedUser);
    }

    /**
     * Autentica un usuario.
     *
     * @param request Datos del usuario
     * @return Token de autenticación
     */
    @Override
    public JwtAuthResponse signIn(UserRequest request) {
        log.info("Autenticando usuario: {}", request);
        // Autenticamos y devolvemos el token
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        var user = userService.loadUserByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) throw new IncorrectPasswordException("La contraseña no es correcta");
        var jwt = jwtService.generateToken(user);
        return JwtAuthResponse.builder().token(jwt).build();
    }
    
    /**
     * Verifica que el conjunto de roles de un usuario no incluye el rol de SUPER_ADMIN.
     *
     * @param request Objeto que contiene los datos del usuario, incluyendo los roles.
     * @throws ResponseStatusException Si el conjunto de roles incluye el rol de SUPER_ADMIN.
     */
    private void verifyRoleDoesntIncludeSuperAdmin(UserRequest request) {
        log.info("Verificando roles del usuario: {}", request);
        if (request.getRoles().contains(Role.SUPER_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se puede añadir un super administrador");
        }
    }
}
