package vives.bancovives.rest.users.controllers;

import vives.bancovives.rest.users.auth.AuthUsersService;
import vives.bancovives.rest.users.auth.AuthUsersServiceImpl;
import vives.bancovives.rest.users.dto.input.UserRequest;
import vives.bancovives.rest.users.dto.input.UserUpdateDto;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.rest.users.exceptions.UserConflict;
import vives.bancovives.rest.users.exceptions.UserNotFound;
import vives.bancovives.rest.users.mappers.UsersMapper;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.security.model.JwtAuthResponse;
import vives.bancovives.security.userauthentication.AuthenticationService;
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
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("${api.version}/users") // Es la ruta del controlador
//@PreAuthorize("hasRole('USER')") // Solo los usuarios pueden acceder
public class UsersRestController {
    private final UsersService usersService;
    private final PaginationLinksUtils paginationLinksUtils;
    private final UsersMapper usersMapper;
    private final AuthenticationService userAuthenticationService;

    @Autowired
    public UsersRestController(UsersService usersService, PaginationLinksUtils paginationLinksUtils, UsersMapper usersMapper, AuthenticationService userAuthenticationService) {
        this.usersService = usersService;
        this.paginationLinksUtils = paginationLinksUtils;
        this.usersMapper = usersMapper;
        this.userAuthenticationService = userAuthenticationService;
    }

    /**
     * Obtiene todos los usuarios
     *
     * @param username  username del usuario
     * @param isDeleted si está borrado o no
     * @param page      página
     * @param size      tamaño
     * @param sortBy    campo de ordenación
     * @param direction dirección de ordenación
     * @param request   petición
     * @return Respuesta con la página de usuarios
     */
    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')") // Solo los admin pueden acceder
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
        // Creamos el objeto de ordenación
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString());
        Page<UserResponse> pageResult = usersService.findAll(username, isDeleted, PageRequest.of(page, size, sort)).map(usersMapper::fromEntityToResponseDto);
        return ResponseEntity.ok()
                .header("link", paginationLinksUtils.createLinkHeader(pageResult, uriBuilder))
                .body(PageResponse.of(pageResult, sortBy, direction));
    }

    /**
     * Obtiene un usuario por su id
     *
     * @param id del usuario, se pasa como parámetro de la URL /{id}
     * @return Usuario si existe
     * @throws UserNotFound si no existe el usuario (404)
     */
    @GetMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')") // Solo los admin pueden acceder
    public ResponseEntity<UserResponse> findById(@PathVariable String id) {
        log.info("findById: id: {}", id);
        return ResponseEntity.ok(usersMapper.fromEntityToResponseDto(usersService.findById(id)));
    }

    /**
     * Crea un nuevo usuario
     *
     * @param userRequest usuario a crear
     * @return Usuario creado
     * @throws UserConflict               si el nombre de usuario o el email ya existen
     * @throws HttpClientErrorException.BadRequest si hay algún error de validación
     */
    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')") // Solo los admin pueden acceder
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("save: userRequest: {}", userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(usersMapper.fromEntityToResponseDto(usersService.save(userRequest)));
    }

    /**
     * Actualiza un usuario
     *
     * @param id          id del usuario
     * @param userUpdate usuario a actualizar
     * @return Usuario actualizado
     * @throws UserNotFound                        si no existe el usuario (404)
     * @throws HttpClientErrorException.BadRequest si hay algún error de validación (400)
     * @throws UserConflict               si el nombre de usuario o el email ya existen (400)
     */
    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')") // Solo los admin pueden acceder
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateDto userUpdate) {
        log.info("update: id: {}, userRequest: {}", id, userUpdate);
        return ResponseEntity.ok(usersMapper.fromEntityToResponseDto(usersService.update(id, userUpdate)));
    }

    /**
     * Borra un usuario
     *
     * @param id id del usuario
     * @return Respuesta vacía
     * @throws UserNotFound si no existe el usuario (404)
     */
    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')") // Solo los admin pueden acceder
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("delete: id: {}", id);
        usersService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

//    /**
//     * Obtiene el usuario actual
//     *
//     * @param //user usuario autenticado
//     * @return Datos del usuario
//     */
//    @GetMapping("/me/profile")
//    //@PreAuthorize("hasRole('USER')") // Solo los usuarios pueden acceder
//    public ResponseEntity<UserInfoResponse> me(/*@AuthenticationPrincipal User user*/) {
//        log.info("Obteniendo usuario");
//        return ResponseEntity.ok(usersService.findById(user.getId()));
//    }

//    /**
//     * Actualiza el usuario actual
//     *
//     * @param //user        usuario autenticado
//     * @param userRequest usuario a actualizar
//     * @return Usuario actualizado
//     * @throws HttpClientErrorException.BadRequest si hay algún error de validación (400)
//     */
//    @PutMapping("/me/profile")
//    //@PreAuthorize("hasRole('USER')") // Solo los usuarios pueden acceder
//    public ResponseEntity<UserResponse> updateMe(/*@AuthenticationPrincipal User user,*/ @Valid @RequestBody UserRequest userRequest) {
//        log.info("updateMe: /*user: {},*/ userRequest: {}"/*, user*/, userRequest);
//        return ResponseEntity.ok(usersService.update(/*user.getId(),*/ userRequest));
//    }

//    /**
//     * Borra el usuario actual
//     *
//     * @param //user usuario autenticado
//     * @return Respuesta vacía
//     */
//    @DeleteMapping("/me/profile")
//    //@PreAuthorize("hasRole('USER')") // Solo los usuarios pueden acceder
//    public ResponseEntity<Void> deleteMe(/*@AuthenticationPrincipal User user*/) {
//        log.info("deleteMe: user: {}"/*, user*/);
//        usersService.deleteById(/*user.getId()*/);
//        return ResponseEntity.noContent().build();
//    }

    /**
     * Manejador de excepciones de Validación: 400 Bad Request
     *
     * @param ex excepción
     * @return Mapa de errores de validación con el campo y el mensaje
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @PostMapping("/signUp")
    public ResponseEntity<JwtAuthResponse> signUpregister(@RequestBody UserRequest user) {
        return ResponseEntity.ok(userAuthenticationService.signUp(user));
    }

    @PostMapping("/signIn")
    public ResponseEntity<JwtAuthResponse> signIn(@RequestBody UserRequest user) {
        log.info("Iniciando sesión");
        return ResponseEntity.ok(userAuthenticationService.signIn(user));
    }

}