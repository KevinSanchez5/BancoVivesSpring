package vives.bancovives.rest.cards.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.repositories.AccountRepository;
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

    Card findById(String id);

    Card findByOwner(String nombre);

    Card save(InputCard card);

    Card deleteById(String id);

    Card updateById(String id, UpdateRequestCard updateCard);
}
