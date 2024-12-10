package vives.bancovives.rest.users.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;
import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.rest.users.exceptions.UserAlreadyExistsException;
import vives.bancovives.rest.users.exceptions.UserNotFoundException;
import vives.bancovives.rest.users.mappers.UsersMapper;
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.security.model.JwtAuthResponse;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("${api.version}/users")
public class UsersController {
    private final UsersService usersService;
    private final PaginationLinksUtils paginationLinksUtils;
    private final UsersMapper usersMapper;

    @Autowired
    public UsersController(
            UsersService usersService, 
            PaginationLinksUtils paginationLinksUtils, 
            UsersMapper usersMapper
    ) {
        this.usersService = usersService;
        this.paginationLinksUtils = paginationLinksUtils;
        this.usersMapper = usersMapper;
    }

    /**
     * Obtiene todos los usuarios según los filtros proporcionados.
     *
     * @param username  Nombre de usuario del usuario.
     * @param isDeleted Si el usuario está eliminado o no.
     * @param page      Número de página.
     * @param size      Tamaño de página.
     * @param sortBy    Campo por el que se ordenará.
     * @param direction Dirección de ordenación (asc o desc).
     * @param request   Solicitud HTTP.
     * @return Respuesta con la página de {@link User}.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> findAll(
            @RequestParam(required = false) Optional<String> username,
            @RequestParam(required = false) Optional<Boolean> isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request
    ) {
        log.info("findAll: username: {}, isDeleted: {}, page: {}, size: {}, sortBy: {}, direction: {}",
                username, isDeleted, page, size, sortBy, direction);
        // Crear el objeto de ordenación
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Crear el constructor de URI para la paginación
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString());
        Page<UserResponse> pageResult = usersService.findAll(username, isDeleted, PageRequest.of(page, size, sort)).map(usersMapper::fromEntityToResponseDto);
        return ResponseEntity.ok()
                .header("link", paginationLinksUtils.createLinkHeader(pageResult, uriBuilder))
                .body(PageResponse.of(pageResult, sortBy, direction));
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario
     * @return {@link User} si existe.
     * @throws vives.bancovives.rest.users.exceptions.UserNotFoundException si no existe el usuario (404)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable String id) {
        log.info("findById: id: {}", id);
        return ResponseEntity.ok(usersMapper.fromEntityToResponseDto(usersService.findById(id)));
    }

    /**
     * Añade un nuevo administrador al sistema.
     *
     * Esta función está destinada a los usuarios con el rol 'SUPER_ADMIN'. Valida que el usuario que se está añadiendo
     * no tenga el rol 'SUPER_ADMIN' y luego guarda al usuario en la base de datos.
     *
     * @param user Usuario a añadir como administrador. Los roles del usuario no deben incluir 'SUPER_ADMIN'.
     * @return Un ResponseEntity si el usuario se añade correctamente como administrador.
     * @throws ResponseStatusException si el usuario que se está añadiendo tiene el rol 'SUPER_ADMIN'.
     */
    @PostMapping("/addAdmin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> addAdmin(@RequestBody UserRequest user) {
        log.info("Agregando admin");
        if (user.getRoles().contains(Role.SUPER_ADMIN)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"No puede añadir un super administrador");
        }
        return ResponseEntity.ok(
                usersMapper.fromEntityToResponseDto(
                        usersService.save(user)
                )
        );
    }
    
    /**
     * Actualiza un usuario.
     *
     * @param id ID del usuario.
     * @param userUpdate Usuario a actualizar.
     * @return Usuario actualizado.
     * @throws UserNotFoundException si no existe
     * @throws HttpClientErrorException.BadRequest si hay algún error de validación
     * @throws UserAlreadyExistsException si el nombre de usuario ya existen
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateDto userUpdate) {
        log.info("update: id: {}, userRequest: {}", id, userUpdate);
        return ResponseEntity.ok(usersMapper.fromEntityToResponseDto(usersService.update(id, userUpdate)));
    }

    /**
     * Borra un usuario.
     *
     * @param id ID del usuario.
     * @return Respuesta vacía.
     * @throws UserNotFoundException si el usuario no existe (404)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("delete: id: {}", id);
        User userFound = usersService.findById(id);
        if (userFound.getRoles().contains(Role.USER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puede eliminar usuarios");
        }else if (userFound.getRoles().contains(Role.SUPER_ADMIN)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puede eliminar super administradores");
        } else usersService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Inicia sesión para un usuario y obtiene un token JWT.
     *
     * Este método permite a un usuario iniciar sesión en la aplicación y obtener un token JWT para realizar operaciones autenticadas.
     *
     * @param user un objeto {@link UserRequest} que contiene los datos de inicio de sesión del usuario.
     *             Debe incluir el nombre de usuario y la contraseña.
     *
     * @return Un objeto {@link JwtAuthResponse} que contiene el token JWT.
     *         Si el nombre de usuario o la contraseña son incorrectos, se devuelve un código de estado 403 (Unauthorized).
     */
    @PostMapping("/signIn")
    public ResponseEntity<JwtAuthResponse> signIn(@RequestBody UserRequest user) {
        log.info("Iniciando sesión");
        return ResponseEntity.ok(usersService.signIn(user));
    }
}