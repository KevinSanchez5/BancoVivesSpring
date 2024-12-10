package vives.bancovives.rest.cards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.dto.output.OutputCard;
import vives.bancovives.rest.cards.mapper.CardMapper;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.service.CardService;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("${api.version}/cards")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class CardController {
    private final CardService cardService;
    private final PaginationLinksUtils paginationLinksUtils;

    @Autowired
    public CardController(CardService cardService, PaginationLinksUtils paginationLinksUtils) {
        this.cardService = cardService;
        this.paginationLinksUtils = paginationLinksUtils;
    }

    /**
     * Obtiene una lista paginada de tarjetas.
     *
     * @param creationDate Fecha de creación de las tarjetas (opcional).
     * @param nombre       Nombre del propietario de las tarjetas (opcional).
     * @param isInactive   Indica si las tarjetas están inactivas (opcional).
     * @param isDeleted    Indica si las tarjetas están eliminadas (opcional).
     * @param page         Número de página (por defecto 0).
     * @param size         Tamaño de la página (por defecto 10).
     * @param sortBy       Campo por el cual ordenar (por defecto "id").
     * @param direction    Dirección de la ordenación (ascendente o descendente, por defecto "asc").
     * @param request      Objeto HttpServletRequest.
     * @return Una respuesta con la lista paginada de tarjetas.
     */
    @Operation(
            summary = "Obtiene una lista paginada de tarjetas",
            description = "Devuelve una lista paginada de tarjetas con filtros opcionales."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de tarjetas obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputCard.class)
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
            )
    })
    @GetMapping
    public ResponseEntity<PageResponse<OutputCard>> getCards(
            @RequestParam(required = false) Optional<LocalDateTime> creationDate,
            @RequestParam(required = false) Optional<String> nombre,
            @RequestParam(required = false) Optional<Boolean> isInactive,
            @RequestParam(required = false) Optional<Boolean> isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request
    ) {
        log.info("Fetching all cards");
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<OutputCard> pageResult = cardService.findAll(creationDate, nombre, isInactive, isDeleted, pageRequest)
                .map(CardMapper::toOutputCard);
        return ResponseEntity.ok()
                .header("link", paginationLinksUtils.createLinkHeader(pageResult, UriComponentsBuilder.fromPath(request.getRequestURL().toString())))
                .body(PageResponse.of(pageResult, sortBy, direction));
    }

    /**
     * Crea una nueva tarjeta.
     *
     * @param inputCard Datos de la tarjeta a crear.
     * @return La tarjeta creada.
     */
    @Operation(
            summary = "Crea una nueva tarjeta",
            description = "Crea una nueva tarjeta con los datos proporcionados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarjeta creada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputCard.class)
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
            )
    })
    @PostMapping
    public ResponseEntity<OutputCard> createCard(@RequestBody @Valid InputCard inputCard) {
        log.info("Creating a new card");
        Card card = cardService.save(inputCard);
        return ResponseEntity.ok(CardMapper.toOutputCard(card));
    }

    /**
     * Obtiene una tarjeta por su ID.
     *
     * @param id ID de la tarjeta.
     * @return La tarjeta correspondiente al ID proporcionado.
     */
    @Operation(
            summary = "Obtiene una tarjeta por su ID",
            description = "Devuelve la tarjeta correspondiente al ID proporcionado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarjeta obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputCard.class)
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
                    description = "Tarjeta no encontrada. El ID proporcionado no existe en la base de datos.",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<OutputCard> getCardById(@PathVariable String id) {
        log.info("Fetching card by ID: {}", id);
        Card card = cardService.findById(id);
        return ResponseEntity.ok(CardMapper.toOutputCard(card));
    }

    /**
     * Obtiene una tarjeta por el nombre del propietario.
     *
     * @param name Nombre del propietario de la tarjeta.
     * @return La tarjeta correspondiente al nombre proporcionado.
     */
    @Operation(
            summary = "Obtiene una tarjeta por el nombre del propietario",
            description = "Devuelve la tarjeta correspondiente al nombre del propietario proporcionado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarjeta obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputCard.class)
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
                    description = "Tarjeta no encontrada. El nombre proporcionado no existe en la base de datos.",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<OutputCard> getCardByName(@PathVariable String name) {
        log.info("Fetching card by name: {}", name);
        Card card = cardService.findByOwner(name);
        return ResponseEntity.ok(CardMapper.toOutputCard(card));
    }

    /**
     * Actualiza una tarjeta por su ID.
     *
     * @param id                ID de la tarjeta a actualizar.
     * @param updateRequestCard Datos de la tarjeta a actualizar.
     * @return La tarjeta actualizada.
     */
    @Operation(
            summary = "Actualiza una tarjeta por su ID",
            description = "Actualiza los datos de una tarjeta existente por su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarjeta actualizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputCard.class)
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
                    description = "Tarjeta no encontrada. El ID proporcionado no existe en la base de datos.",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<OutputCard> updateCard(
            @PathVariable String id,
            @RequestBody @Valid UpdateRequestCard updateRequestCard
    ) {
        log.info("Updating card by ID: {}", id);
        Card updatedCard = cardService.updateById(id, updateRequestCard);
        return ResponseEntity.ok(CardMapper.toOutputCard(updatedCard));
    }

    /**
     * Elimina una tarjeta por su ID.
     *
     * @param id ID de la tarjeta a eliminar.
     * @return La tarjeta eliminada.
     */
    @Operation(
            summary = "Elimina una tarjeta por su ID",
            description = "Elimina una tarjeta existente por su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarjeta eliminada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputCard.class)
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
                    description = "Tarjeta no encontrada. El ID proporcionado no existe en la base de datos.",
                    content = @Content(mediaType = "application/json")
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<OutputCard> deleteCard(@PathVariable String id) {
        log.info("Deleting card by ID: {}", id);
        Card deletedCard = cardService.deleteById(id);
        return ResponseEntity.ok(CardMapper.toOutputCard(deletedCard));
    }
}