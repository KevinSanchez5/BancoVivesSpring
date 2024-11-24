package vives.bancovives.rest.cards.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.model.Card;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CardService {
    Page<Card> findAll(
            Optional<LocalDateTime> creationDate,
            Optional<String> nombre,
            Optional<Boolean> isInactive,
            Optional<Boolean> isDeleted,
            Pageable pageable
    );

    Card findById(UUID id);

    Card findByOwner(String nombre);

    Card save(InputCard card);

    Card deleteById(UUID id);

    Card updateById(UUID id, UpdateRequestCard updateCard);
}
