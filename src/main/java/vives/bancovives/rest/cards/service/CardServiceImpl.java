package vives.bancovives.rest.cards.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.exceptions.CardDoesNotExistException;
import vives.bancovives.rest.cards.mapper.CardMapper;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.repository.CardsRepository;
import vives.bancovives.rest.cards.utile.CreditCardGenerator;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@CacheConfig(cacheNames = {"cards"})
public class CardServiceImpl implements CardService {

    private final CardsRepository repository;

    @Autowired
    public CardServiceImpl(CardsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Card> findAll(
            Optional<LocalDateTime> creationDate,
            Optional<String> nombre,
            Optional<Boolean> isInactive,
            Optional<Boolean> isDeleted,
            Pageable pageable
    ) {
        log.info("Buscando todas las tarjetas");

        // Criterio de búsqueda por el titular de la tarjeta
        Specification<Card> cardOwnerSpec = (root, query, criteriaBuilder) ->
                nombre.map(owner -> criteriaBuilder.like(criteriaBuilder.lower(root.get("cardOwner")), "%" + owner.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Card> isInactiveSpec = (root, query, criteriaBuilder) ->
                isInactive.map(inactive -> criteriaBuilder.equal(root.get("isInactive"), inactive))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        // Criterio de búsqueda por si la tarjeta está eliminada o no
        Specification<Card> isDeletedSpec = (root, query, criteriaBuilder) ->
                isDeleted.map(deleted -> criteriaBuilder.equal(root.get("isDeleted"), deleted))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Card> criteria =
                Specification.where(cardOwnerSpec)
                        .and(isInactiveSpec)
                        .and(isDeletedSpec);

        return repository.findAll(criteria, pageable);
    }

    @Override
    @Cacheable(key = "#id")
    public Card findById(UUID id) {
        log.info("Buscando la tarjeta con id: " + id);
        return repository.findById(id).orElseThrow(
                () -> new CardDoesNotExistException("La tarjeta con id: " + id + " no existe"));
    }


    @Override
    public Card findByOwner(String nombre) {
        log.info("Buscando el producto con nombre: " + nombre);
        return repository.findByCardOwner(nombre.trim().toUpperCase()).orElseThrow(
                () -> new CardDoesNotExistException("La tarjeta con nombre: " + nombre + " no existe"));

    }

    @Override
    @CachePut(key = "#result.id")
    public Card save(InputCard card) {
        log.info("Guardando tarjeta: " + card);
        Card result = CardMapper.toCard(card);
        result.setCardNumber(CreditCardGenerator.generateCardNumber());
        result.setExpirationDate(CreditCardGenerator.generateExpirationDate());
        result.setCvv(CreditCardGenerator.generateCVV());
        return repository.save(result);
    }

    @Override
    @CacheEvict(key = "#id")
    public Card deleteById(UUID id) {
        log.info("Eliminando la tarjeta con id: " + id);
        Card result = findById(id);
        result.setIsDeleted(true);
        result.setLastUpdate(LocalDateTime.now());
        return result;
    }

    @Override
    @CachePut(key = "#id")
    public Card updateById(UUID id, UpdateRequestCard updateCard) {
        log.info("Actualizando la tarjeta con id: " + id);
        Card existingCard = findById(id);
        Card updatedCard = CardMapper.toCard(updateCard, existingCard);
        return repository.save(updatedCard);
    }
}