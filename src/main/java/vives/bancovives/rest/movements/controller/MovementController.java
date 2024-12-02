package vives.bancovives.rest.movements.controller;


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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.output.MovementResponseDto;
import vives.bancovives.rest.movements.dtos.input.MovementUpdateDto;
import vives.bancovives.rest.movements.model.Movement;
import vives.bancovives.rest.movements.services.MovementService;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

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

    @RequestMapping()
    public ResponseEntity<PageResponse<Movement>> getAllMovements(
            @RequestParam(required = false) Optional<String> movementType,
            @RequestParam(required = false) Optional<String> iban,
            @RequestParam(required = false) Optional<String> clientDni,
            @RequestParam(required = false) Optional<String> fecha,
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
        Page<Movement> pageResult = movementService.findAll(movementType, iban, clientDni, fecha, isDeleted, PageRequest.of(page, size, sort));
    return ResponseEntity.ok()
                .header("link", paginationLinksUtils.createLinkHeader(pageResult, uriBuilder))
                .body(PageResponse.of(pageResult, sortBy, direction));
    }

    @RequestMapping("/{id}")
    public ResponseEntity<MovementResponseDto> getMovementById(@PathVariable ObjectId id) {
        log.info("Get movement by id: {}", id);
        return ResponseEntity.ok(movementService.findById(id));
    }

    @PostMapping()
    public ResponseEntity<MovementResponseDto> createMovement(@Valid @RequestBody MovementCreateDto movementDto) {
        log.info("Create movement: {}", movementDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.save(movementDto));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<MovementResponseDto> updateMovement(@PathVariable ObjectId id, @RequestBody MovementCreateDto movement) {
        log.info("Update movement with id: {} and movement: {}", id, movement);
        return ResponseEntity.ok(movementService.update(id, movement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovement(@PathVariable ObjectId id) {
        log.info("Delete movement with id: {}", id);
        movementService.cancelMovement(id);
        return ResponseEntity.noContent().build();
    }

}
