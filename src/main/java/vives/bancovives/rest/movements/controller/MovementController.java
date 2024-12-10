package vives.bancovives.rest.movements.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.output.MovementResponseDto;
import vives.bancovives.rest.movements.services.MovementService;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/${api.version}/movements")
@Slf4j
public class MovementController {
    private final MovementService movementService;
    private final PaginationLinksUtils paginationLinksUtils;


    @Autowired
    public MovementController(MovementService movementService, PaginationLinksUtils paginationLinksUtils) {
        this.movementService = movementService;
        this.paginationLinksUtils = paginationLinksUtils;
    }

    /**
     * Busca todos los movimientos filtrados por diferentes parametros
     * @param movementType el tipo de movimiento
     * @param iban  iban de referencia de la operacion
     * @param fecha fecha en el que se hizo la operacion con formato aaaa-mm-dd
     * @param clientOfReferenceDni dni del cliente de referencia
     * @param isDeleted si el movimiento ha sido eliminado
     * @param page numeor de pagina
     * @param size tamaño de la pagina
     * @param sortBy campo por el que se ordena
     * @param direction direccion de la ordenacion
     * @param request peticion http
     * @return Un response entity con el codigo de respuesta y la lista de movimientos
     */
    @Operation(summary = "Busca todos los movimientos filtrados por diferentes parametros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devuelve una lista de movimientos", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class)))
    })
    @RequestMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PageResponse<MovementResponseDto>> getAllMovements(
            @RequestParam(required = false) Optional<String> movementType,
            @RequestParam(required = false) Optional<String> iban,
            @RequestParam(required = false) Optional<String> fecha,
            @RequestParam(required = false) Optional<String> clientOfReferenceDni,
            @RequestParam(required = false, defaultValue = "false")Optional<Boolean> isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request
            ) {
        log.info("Buscando todos los movimientos");
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString());
        Page<MovementResponseDto> pageResult = movementService.findAll(movementType, iban, fecha, clientOfReferenceDni,Optional.empty(),  isDeleted, PageRequest.of(page, size, sort));
    return ResponseEntity.ok()
                .header("link", paginationLinksUtils.createLinkHeader(pageResult, uriBuilder))
                .body(PageResponse.of(pageResult, sortBy, direction));
    }

    /**
     * Busca un movimiento por su id
     * @param id id del movimiento
     * @return Un response entity con el codigo de respuesta y el movimiento
     */
    @Operation(summary = "Busca un movimiento por su id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devuelve un movimiento", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el movimiento", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class)))
    })
    @RequestMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<MovementResponseDto> getMovementById(@PathVariable ObjectId id) {
        log.info("Get movement by id: {}", id);
        return ResponseEntity.ok(movementService.findById(id));
    }

    /**
     * Crea un movimiento
     * @param principal usuario que realiza la operacion
     * @param movementDto datos del movimiento
     * @return Un response entity con el codigo de respuesta y el movimiento creado
     */
    @Operation(summary = "Crea un movimiento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Devuelve el movimiento creado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Error en los datos del movimiento", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la cuenta o tarjeta relacionada con el movimiento", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class)))
    })
    @PostMapping()
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<MovementResponseDto> createMovement(Principal principal, @Valid @RequestBody MovementCreateDto movementDto) {
        log.info("Create movement: {}", movementDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.save(principal, movementDto));
    }

    /**
     * Actualiza un movimiento
     * @param id id del movimiento
     * @param movement datos del movimiento
     * @return Un response entity con el codigo de respuesta y el movimiento actualizado
     */
    @Operation(summary = "Actualiza un movimiento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devuelve el movimiento actualizado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Error en los datos del movimiento", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el movimiento o la cuenta o tarjeta realaciona do con el movimiento", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class)))
    })
    @PutMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<MovementResponseDto> updateMovement(@PathVariable ObjectId id, @RequestBody MovementCreateDto movement) {
        log.info("Update movement with id: {} and movement: {}", id, movement);
        return ResponseEntity.ok(movementService.update(id, movement));
    }

    /**
     * Elimina un movimiento
     * @param principal usuario que realiza la operacion
     * @param id id del movimiento
     * @return Un response entity con el codigo de respuesta
     */
    @Operation(summary = "Elimina un movimiento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Movimiento eliminado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el movimiento", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteMovement(Principal principal, @PathVariable ObjectId id) {
        log.info("Delete movement with id: {}", id);
        movementService.cancelMovement(principal, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Añade interes manualmente a una cuenta realizado por administrador
     * @param movementDto datos del movimiento
     * @return Un response entity con el codigo de respuesta y el movimiento creado
     */
    @Operation(summary = "Añade interes manualmente a una cuenta realizado por administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Devuelve el movimiento creado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Error en los datos del movimiento", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la cuenta relacionada con el movimiento", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class)))
    })
    @PostMapping("/addinterest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<MovementResponseDto> addInterest(@RequestBody MovementCreateDto movementDto) {
        log.info("Añadiendo interes manualmente a una cuenta");
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.addInterest(movementDto));
    }

    /**
     * Busca los movimientos de un usuario
     * @param principal usuario que realiza la operacion
     * @param page numero de la pagina
     * @param size tamaño de la pagina
     * @param sortBy campo por el que se ordena
     * @param direction direccion de la ordenacion
     * @param request peticion http
     */
    @Operation(summary = "Busca los movimientos de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devuelve una lista de movimientos", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content(schema = @Schema(implementation = MovementResponseDto.class)))
    })
    @GetMapping("/myMovements")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PageResponse<MovementResponseDto>> findMe(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request){
        log.info("Buscando sus movientos");
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString());
        Page<MovementResponseDto> pageResult = movementService.findMyMovements(principal, PageRequest.of(page, size, sort));
        return ResponseEntity.ok().header("link", paginationLinksUtils.createLinkHeader(pageResult, uriBuilder))
                .body(PageResponse.of(pageResult, sortBy, direction));
    }

}
