package vives.bancovives.rest.cards.controller;


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
public class CardController {
    private final CardService cardService;

    private final PaginationLinksUtils paginationLinksUtils;


    @Autowired
    public CardController(CardService cardService, PaginationLinksUtils paginationLinksUtils) {
        this.cardService = cardService;
        this.paginationLinksUtils = paginationLinksUtils;
    }


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

    @PostMapping
    public ResponseEntity<OutputCard> createCard(@RequestBody @Valid InputCard inputCard) {
        log.info("Creating a new card");
        Card card = cardService.save(inputCard);
        return ResponseEntity.status(HttpStatus.CREATED).body(CardMapper.toOutputCard(card));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OutputCard> getCardById(@PathVariable String id) {
        log.info("Fetching card by ID: {}", id);
        Card card = cardService.findById(id);
        return ResponseEntity.ok(CardMapper.toOutputCard(card));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<OutputCard> getCardByName(@PathVariable String name) {
        log.info("Fetching card by name: {}", name);
        Card card = cardService.findByOwner(name);
        return ResponseEntity.ok(CardMapper.toOutputCard(card));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OutputCard> updateCard(
            @PathVariable String id,
            @RequestBody @Valid UpdateRequestCard updateRequestCard
    ) {
        log.info("Updating card by ID: {}", id);
        Card updatedCard = cardService.updateById(id, updateRequestCard);
        return ResponseEntity.ok(CardMapper.toOutputCard(updatedCard));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OutputCard> deleteCard(@PathVariable String id) {
        log.info("Deleting card by ID: {}", id);
        Card deletedCard = cardService.deleteById(id);
        return ResponseEntity.ok(CardMapper.toOutputCard(deletedCard));
    }
}