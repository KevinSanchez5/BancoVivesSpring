package vives.bancovives.rest.cards.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vives.bancovives.rest.accounts.exception.AccountException;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.service.AccountService;
import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.exceptions.CardDoesNotExistException;
import vives.bancovives.rest.cards.exceptions.CardException;
import vives.bancovives.rest.cards.exceptions.CardIbanInUse;
import vives.bancovives.rest.cards.mapper.CardMapper;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.repository.CardsRepository;
import vives.bancovives.rest.products.cardtype.model.CardType;
import vives.bancovives.rest.products.cardtype.service.CardTypeService;
import vives.bancovives.utils.card.CreditCardGenerator;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@CacheConfig(cacheNames = {"cards"})
public class CardServiceImpl implements CardService {

    private final CardsRepository repository;
    private final CardTypeService repositoryCardType;
    private final AccountService accountRepository;

    @Autowired
    public CardServiceImpl(CardsRepository repository, CardTypeService repositoryCardType, AccountService accountRepository) {
        this.repository = repository;
        this.repositoryCardType = repositoryCardType;
        this.accountRepository = accountRepository;
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
    public Card findById(String id) {
        log.info("Buscando la tarjeta con id: " + id);
        return repository.findByPublicId(id).orElseThrow(
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

    public Account existingAccount(String iban) {
        return accountRepository.findByIban(iban);
    }

    public Account notDeleteAccount(Account account) {
        if (account.isDeleted()) {
            throw new AccountException("Cuenta borrada");
        }
        return account;
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

    public Account validationAccount(String iban) {
        Account c = existingAccount(iban);
        notDeleteAccount(c);
        return c;
    }

    public void isIbanInUse(Card card) {
        String iban = card.getAccount().getIban();
        if (repository.existsByAccount_Iban(iban)) {
            throw new CardIbanInUse("El IBAN: " + iban + " esta ya en uso.");
        }
    }

    @Override
    @CachePut(key = "#result.id")
    public Card save(InputCard card) {
        log.info("Saving card: " + card);
        Account account = validationAccount(card.getAccount());
        CardType type = validation(card.getCardTypeName());
        Card result = CardMapper.toCard(card, type, account);
        isIbanInUse(result);
        CreditCardGenerator.generateCardDetails(result);
        return repository.save(result);
    }

    @Override
    @CacheEvict(key = "#id")
    public Card deleteById(String id) {
        log.info("Eliminando la tarjeta con id: " + id);
        Card result = findById(id);
        result.setIsDeleted(true);
        result.setLastUpdate(LocalDateTime.now());
        return repository.save(result);
    }

    @Override
    @CachePut(key = "#id")
    public Card updateById(String id, UpdateRequestCard updateCard) {
        log.info("Actualizando la tarjeta con id: " + id);
        Card existingCard = findById(id);
        Card updatedCard = CardMapper.toCard(updateCard, existingCard);
        return repository.save(updatedCard);
    }

    @Scheduled(cron ="0 0 0 * * ?")
    @Transactional
    public void resetSpentAmountsInBatches() {
        log.info("Reseteando los montos gastados de las tarjetas en lotes");
        int pageSize = 200;
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<Card> page;

        do {
            page = repository.findAll(pageable);
            resetSpentAmountsForCards(page.getContent());
            pageable = page.nextPageable();
        } while (!page.isLast());
    }

    private void resetSpentAmountsForCards(List<Card> cards) {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();

        for (Card card : cards) {
            card.setSpentToday(0);
            if (now.getDayOfWeek() == firstDayOfWeek) {
                card.setSpentThisWeek(0);
            }
            if (now.getDayOfMonth() == 1) {
                card.setSpentThisMonth(0);
            }
        }
        repository.saveAll(cards);
    }
}