package vives.bancovives.rest.clients.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.exceptions.ClientBadRequest;
import vives.bancovives.rest.clients.exceptions.ClientConflict;
import vives.bancovives.rest.clients.exceptions.ClientNotFound;
import vives.bancovives.rest.clients.service.ClientService;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestionar los clientes
 */
@RestController
@RequestMapping("${api.version}/clients")
@Slf4j
@Tag(name = "API de clientes", description="API para la gestion de clientes")
public class ClientController {

    private final ClientService clientService;
    private final PaginationLinksUtils paginationLinksUtils;

    /**
     * Constructor para el controlador ClientController
     * @param clientService Servicio para gestionar los clientes y sus datos
     * @param paginationLinksUtils Utilidad para crear los enlaces de paginacion
     */
    @Autowired
    public ClientController(ClientService clientService, PaginationLinksUtils paginationLinksUtils) {
        this.clientService = clientService;
        this.paginationLinksUtils = paginationLinksUtils;
    }

    /**
     * Recupera una lista de {@link ClientResponseDto} basada en filtros propocionados y parámetros de paginación
     *
     * @param dni           Dni del cliente a buscar
     * @param completeName  Nombre del cliente por el que se quiere filtar
     * @param email         Email del cliente
     * @param street        Calle
     * @param city          Ciudad
     * @param validated     Clientes cuyos datos han sido validados o no
     * @param isDeleted     Clientes borrados o no
     * @param page          Numero de pagina que se quiere adquirir
     * @param size          Numero de elementos que hay en la pagina
     * @param sortBy        Campo por el que se ordena
     * @param direction     Direccion de ordenamiento
     * @param request       La solicitud http
     * @return              Un {@link ResponseEntity} que contiene un {@link PageResponse} de entidades {@link ClientResponseDto}
     */
    @Operation(
            summary = "Obriene una lista paginada de clientes",
            description = "Obtiene una lista de clientes filtradas o no por una serie de parámetros opcionales como DNI, nombre completo, email, calle, ciudad, estadp de validación y si están borrados.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe autenticarse para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accesso prohibido. El usuario no tiene permisos suficientes para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PageResponse<ClientResponseDto>> getClients(
            @Parameter(description = "Dni del cliente a buscar")
            @RequestParam(required = false) Optional<String> dni,
            @Parameter(description = "Nombre completo del cliente")
            @RequestParam(required = false) Optional<String> completeName,
            @Parameter(description = "Email del cliente")
            @RequestParam(required = false) Optional<String> email,
            @Parameter(description = "Calle del cliente")
            @RequestParam(required = false) Optional<String> street,
            @Parameter(description = "Ciudad del cliente")
            @RequestParam(required = false) Optional<String> city,
            @Parameter(description = "Si el cliente está validado o no")
            @RequestParam(required = false) Optional<Boolean> validated,
            @Parameter(description = "Si el cliente está borrado o no (por defecto false)")
            @RequestParam(required = false, defaultValue = "false") Optional<Boolean> isDeleted,
            @Parameter(description = "Número de página a obtener")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Número de elementos por página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el que se ordena la lista")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (ascendente o descendente)")
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request
    ){
        log.info("Recuperando todos los clientes");
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString());
        Page<ClientResponseDto> pageResult = clientService.findAll(dni, completeName, email, street, city, validated, isDeleted, PageRequest.of(page, size, sort));
        return ResponseEntity.ok()
                .header("link", paginationLinksUtils.createLinkHeader(pageResult, uriBuilder))
                .body(PageResponse.of(pageResult, sortBy, direction));
    }

    /**
     * Recupera un Cliente a partir de su publicId
     *
     * @param id el id publico del cliente
     * @return  Un {@link ResponseEntity} con la informacion del cliente a buscar {@link ClientResponseDto}
     * @throws ClientNotFound si el cliente con ese id no se encuentra en la base de datos
     */
    @Operation(
            summary = "Obtiene un cliente por su id público",
            description = "Devuelve los detalles de un cliente en especifico basado en su id público")
    @ApiResponses( value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientResponseDto.class)
                    )

            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado. El ID proporcionado no existe en la base de datos",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe autenticarse para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso prohibido. El usuario no tiene permisos suficientes para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientResponseDto> getClientById(@PathVariable String id) {
        log.info("Recuperando cliente con id: {}", id);
        return ResponseEntity.ok(clientService.findById(id));
    }


    /**
     * Crea un nuevo cliente y su usuario a partir de un {@link ClientCreateDto}
     *
     * @param createDto datos del cliente y usuario enlazado que se quiere crear
     * @return Un {@link ResponseEntity} con la informacion de el nuevo cliente creado dentro de un {@link ClientResponseDto}
     * @throws ClientBadRequest si existe algun error con los datos del cliente
     * @throws ClientConflict si ya existe un cliente con el dni o email que se quiere almacenar
     */
    @Operation(
            summary = "Crea un nuevo cliente y su usuario",
            description = "Crea un nuevo cliente y su usuario a partir de un objeto ClientCreateDto, los datos son comprobados")
    @ApiResponses( value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en los datos del cliente. Los datos proporcionados no son válidos",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El cliente con el dni o email proporcionado ya existe en la base de datos",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping
    public ResponseEntity<ClientResponseDto> createClient(@RequestBody @Valid ClientCreateDto createDto) {
        log.info("Creando un nuevo cliente");
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.save(createDto));
    }

    /**
     * Actualiza un cliente por su id
     *
     * @param id                 el ID del cliente a actualizar
     * @param updateDto          Un {@link ClientUpdateDto} con los datos del cliente que se quieren modificar
     * @return                   Un {@link ResponseEntity} con la informacion de los nuevos datos del cliente actualizado en un {@link ClientResponseDto}
     * @throws ClientNotFound    Si el cliente que se quiere actualizar no se encuentra
     * @throws ClientBadRequest  Si los datos a actulizar son incorrectos
     * @throws ClientConflict    Si ya existe un cliente con el dni o email que se quiere actualizar
     */
    @Operation(
            summary = "Actualiza un cliente",
            description = "Actualiza un cliente dado su public Id e inserta los nuevos datos a traves de un ClientUpdateDto"
    )
    @ApiResponses( value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado. El ID proporcionado no existe en la base de datos",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en los datos del cliente. Los datos proporcionados no son válidos",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El cliente con el dni o email proporcionado ya existe en la base de datos",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe autenticarse para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso prohibido. El usuario no tiene permisos suficientes para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientResponseDto> updateClient(@PathVariable String id, @RequestBody ClientUpdateDto updateDto) {
        log.info("Actualizando cliente con id: {}", id);
        return ResponseEntity.ok(clientService.update(id, updateDto));
    }

    /**
     * Elimina un cliente por su ID
     *
     * @param id            El id del cliente que se quiere eliminar
     * @param deleteData    Un {@link Optional<Boolean>} indica si se quiere borrar los datos del cliente
     * @return              Un {@link ResponseEntity} con la información del tipo de cuenta actualizado dentro de un objeto {@link ClientResponseDto}
     * @throws ClientNotFound si el cliente a borrar no se encuentra
     */
    @Operation(
            summary = "Elimina un cliente",
            description = "Elimina un cliente dado su ID. Puede optar por borrar los datos del cliente o indicar si solo es un borrado lógico."
    )
    @ApiResponses( value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente eliminado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado. El ID proporcionado no existe en la base de datos",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe autenticarse para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso prohibido. El usuario no tiene permisos suficientes para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientResponseDto> deleteClient(@PathVariable String id, @RequestParam(defaultValue = "false") Optional<Boolean> deleteData) {
        log.info("Borrando cliente con id: {}", id);
        return ResponseEntity.ok(clientService.deleteByIdLogically(id, deleteData));
    }

    /**
     * Actualiza el estado de un cliente a validado, indicando que sus datos son correctos
     *
     * @param id    Id del cliente por el que se busca
     * @return      Un {@link ResponseEntity} con un objeto {@link ClientResponseDto} con los datos del cliente validado
     * @throws ClientNotFound si no se encuentra el cliente
     */
    @Operation(
            summary = "Valida los datos del cliente",
            description = "Cambia el estado del cliente a validado indicando que sus datos son correctos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente validado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado. El ID proporcionado no existe en la base de datos",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe autenticarse para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso prohibido. El usuario no tiene permisos suficientes para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PutMapping("/{id}/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientResponseDto> validateClient(@PathVariable String id) {
        log.info("Validando cliente con id: {}", id);
        return ResponseEntity.ok(clientService.validateClient(id));
    }

    /**
     * Muestra la informacion del cliente que ha iniciado sesion
     * @param principal Usuario que ha iniciado sesion
     * @return Un {@link ResponseEntity} con un objeto {@link ClientResponseDto} con los datos del cliente que ha iniciado sesion
     */
    @Operation(
            summary = "Devuelve los datos del cliente que usa la aplicacion",
            description = "Devuelve los datos del cliente que ha iniciado sesion en la aplicacion"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Información devuelta exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe autenticarse para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso prohibido. El usuario no tiene permisos suficientes para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientResponseDto> findMe(Principal principal){
        log.info("Buscando su informacion");
        return ResponseEntity.ok(clientService.findMe(principal));

    }

    /**
     * Actualiza la imagen del dni del cliente
     * @param principal Usuario que ha iniciado sesion
     * @param file Archivo de imagen del dni
     * @return  Un {@link ResponseEntity} con un objeto {@link Map} con la url de la imagen actualizada
     */
    @Operation(
            summary = "Actualiza la imagen del dni del cliente",
            description = "Actualiza la imagen del dni del cliente que ha iniciado sesion"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Imagen actualizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe autenticarse para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso prohibido. El usuario no tiene permisos suficientes para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PatchMapping(value= "/dniImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadDniPicture(Principal principal, @RequestPart("file") MultipartFile file) {
        log.info("Actualizando imagen del dni del clietne con username: {}", principal.getName());
        return ResponseEntity.ok(clientService.storeImage(principal,  file, "dniPicture"));
    }

    /**
     * Actualiza la imagen de perfil del cliente
     * @param principal Usuario que ha iniciado sesion
     * @param file Archivo de imagen de perfil
     * @return  Un {@link ResponseEntity} con un objeto {@link Map} con la url de la imagen actualizada
     */
    @Operation(
            summary = "Actualiza la imagen de perfil del cliente",
            description = "Actualiza la imagen de perfil del cliente que ha iniciado sesion"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Imagen actualizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe autenticarse para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso prohibido. El usuario no tiene permisos suficientes para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PatchMapping(value= "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadPhoto(Principal principal,  @RequestPart("file") MultipartFile file) {
        log.info("Actualizando imagen de perfil del cliente con username: {}", principal.getName());
        return ResponseEntity.ok(clientService.storeImage(principal, file, "photo"));
    }

    /**
     * Exporta los datos del cliente en formato JSON
     * @param principal Usuario que ha iniciado sesion
     * @return  Un {@link ResponseEntity} con un objeto {@link Resource} con los datos del cliente exportados
     */
    @Operation(
            summary = "Exporta los datos del cliente en formato JSON",
            description = "Exporta los datos del cliente que ha iniciado sesion en formato JSON"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Datos exportados exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Resource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. El usuario debe autenticarse para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso prohibido. El usuario no tiene permisos suficientes para acceder a este recurso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/exportMe")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Resource> exportClient(Principal principal) {
        Resource clientJson = clientService.exportMeAsJson(principal);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + clientJson.getFilename() + "\"")
                .body(clientJson);
    }

}