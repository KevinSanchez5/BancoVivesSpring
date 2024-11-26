package vives.bancovives.rest.clients.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.service.ClientService;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.util.Optional;

@RestController
@RequestMapping("${api.version}/clients")
@Slf4j
public class ClientController {

    private final ClientService clientService;
    private final PaginationLinksUtils paginationLinksUtils;

    @Autowired
    public ClientController(ClientService clientService, PaginationLinksUtils paginationLinksUtils) {
        this.clientService = clientService;
        this.paginationLinksUtils = paginationLinksUtils;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ClientResponseDto>> getClients(
            @RequestParam(required = false) Optional<String> dni,
            @RequestParam(required = false) Optional<String> completeName,
            @RequestParam(required = false) Optional<String> email,
            @RequestParam(required = false) Optional<String> street,
            @RequestParam(required = false) Optional<String> city,
            @RequestParam(required = false) Optional<Boolean> validated,
            @RequestParam(required = false, defaultValue = "false") Optional<Boolean> isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
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

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDto> getClientById(@PathVariable String id) {
        log.info("Recuperando cliente con id: {}", id);
        return ResponseEntity.ok(clientService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ClientResponseDto> createClient(@RequestBody @Valid ClientCreateDto createDto) {
        log.info("Creando un nuevo cliente");
        return ResponseEntity.ok(clientService.save(createDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDto> updateClient(@PathVariable String id, @RequestBody ClientUpdateDto updateDto) {
        log.info("Actualizando cliente con id: {}", id);
        return ResponseEntity.ok(clientService.update(id, updateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ClientResponseDto> deleteClient(@PathVariable String id, @RequestParam(defaultValue = "false") Optional<Boolean> deleteData) {
        log.info("Borrando cliente con id: {}", id);
        return ResponseEntity.ok(clientService.deleteByIdLogically(id, deleteData));
    }

    @PutMapping("/{id}/validate")
    public ResponseEntity<ClientResponseDto> validateClient(@PathVariable String id) {
        log.info("Validando cliente con id: {}", id);
        return ResponseEntity.ok(clientService.validateClient(id));
    }
}