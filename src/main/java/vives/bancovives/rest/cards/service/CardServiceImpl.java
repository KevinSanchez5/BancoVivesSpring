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
import vives.bancovives.rest.accounts.exception.AccountException;

import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.repositories.AccountRepository;
import vives.bancovives.rest.accounts.service.AccountService;
import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.exceptions.CardDoesNotExistException;
import vives.bancovives.rest.cards.exceptions.CardException;
import vives.bancovives.rest.cards.mapper.CardMapper;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.repository.CardsRepository;
import vives.bancovives.rest.products.cardtype.model.CardType;
import vives.bancovives.rest.products.cardtype.service.CardTypeService;
import vives.bancovives.utils.card.CreditCardGenerator;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@CacheConfig(cacheNames = {"cards"})
public class CardServiceImpl implements CardService {

    private final CardsRepository repository;
    private final AccountService accountRepository;
    private final CardTypeService repositoryCardType;

    @Autowired
    public CardServiceImpl(CardsRepository repository, AccountService accountRepository, CardTypeService repositoryCardType) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.repositoryCardType = repositoryCardType;
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
        log.info("Buscando la tarjeta con id: {}", id);
        return repository.findById(id).orElseThrow(
                () -> new CardDoesNotExistException("La tarjeta con id: " + id + " no existe"));
    }


    @Override
    public Card findByOwner(String nombre) {
        log.info("Buscando el producto con nombre: " + nombre);
        return repository.findByCardOwner(nombre.trim().toUpperCase()).orElseThrow(
                () -> new CardDoesNotExistException("La tarjeta con nombre: " + nombre + " no existe"));

    }

    public CardType existing(String name) {
        return repositoryCardType.findByName(name);
    }

    public Account notDeleteAccount(Account account) {
        if (account.isDeleted()) {
            throw new AccountException("Cuenta borrada");
        }
        return account;
    }
    public Account existingAccount(String iban) {
        return accountRepository.findByIban(iban);
    }

    public CardType notDelete(CardType cardType) {
        if (cardType.getIsDeleted()) {
            throw new CardException("El tipo de tarjeta esta borrado");
        }
        return cardType;
    }


    public CardType validation(String id) {
        CardType c = existing(id);
        notDelete(c);
        return c;
    }
    public Account accountValidation(String iban){
        Account a = existingAccount(iban);
        notDeleteAccount(a);
        return a;
    }

    @Override
    @CachePut(key = "#result.id")
    public Card save(InputCard card) {
        log.info("Guardando tarjeta: " + card);
        Account account = accountValidation(card.getAccount());
        CardType type = validation(card.getCardTypeName());
        Card result = CardMapper.toCard(card, type,account);
        CreditCardGenerator.generateCardDetails(result);
        return repository.save(result);
    }

    @Override
    @CacheEvict(key = "#id")
    public Card deleteById(UUID id) {
        log.info("Eliminando la tarjeta con id: {}", id);
        Card result = findById(id);
        result.setIsDeleted(true);
        result.setLastUpdate(LocalDateTime.now());
        return result;
    }

    @Override
    @CachePut(key = "#id")
    public Card updateById(UUID id, UpdateRequestCard updateCard) {
        log.info("Actualizando la tarjeta con id: {}", id);
        Card existingCard = findById(id);
        Card updatedCard = CardMapper.toCard(updateCard, existingCard);
        return repository.save(updatedCard);
    }
}