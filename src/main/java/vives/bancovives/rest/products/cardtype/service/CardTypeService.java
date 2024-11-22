package vives.bancovives.rest.products.cardtype.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.products.cardtype.dto.input.NewCardType;
import vives.bancovives.rest.products.cardtype.dto.input.UpdatedCardType;
import vives.bancovives.rest.products.cardtype.model.CardType;

import java.util.Optional;

public interface CardTypeService {
    Page<CardType> findAll(
        Optional<Boolean> isDeleted,
        Optional<String> name,
        Pageable pageable
    );
    CardType findById(String id);
    CardType findByName(String name);
    CardType save(NewCardType newCardType);
    CardType delete(String id);
    CardType update(String id, UpdatedCardType updatedCardType);
}
