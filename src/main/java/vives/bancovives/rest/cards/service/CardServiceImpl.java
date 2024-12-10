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

/**
 * Implementación del servicio de tarjetas.
 */
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

    /**
     * Busca todas las tarjetas con los filtros opcionales.
     *
     * @param creationDate Fecha de creación de las tarjetas (opcional).
     * @param nombre       Nombre del propietario de las tarjetas (opcional).
     * @param isInactive   Indica si las tarjetas están inactivas (opcional).
     * @param isDeleted    Indica si las tarjetas están eliminadas (opcional).
     * @param pageable     Información de paginación.
     * @return Una página de tarjetas.
     */
    @Override
    public Page<Card> findAll(
            Optional<LocalDateTime> creationDate,
            Optional<String> nombre,
            Optional<Boolean> isInactive,
            Optional<Boolean> isDeleted,
            Pageable pageable
    ) {
        log.info("Buscando todas las tarjetas");

        Specification<Card> cardOwnerSpec = (root, query, criteriaBuilder) ->
                nombre.map(owner -> criteriaBuilder.like(criteriaBuilder.lower(root.get("cardOwner")), "%" + owner.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Card> isInactiveSpec = (root, query, criteriaBuilder) ->
                isInactive.map(inactive -> criteriaBuilder.equal(root.get("isInactive"), inactive))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Card> isDeletedSpec = (root, query, criteriaBuilder) ->
                isDeleted.map(deleted -> criteriaBuilder.equal(root.get("isDeleted"), deleted))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Card> criteria =
                Specification.where(cardOwnerSpec)
                        .and(isInactiveSpec)
                        .and(isDeletedSpec);

        return repository.findAll(criteria, pageable);
    }

    /**
     * Busca una tarjeta por su ID.
     *
     * @param id ID de la tarjeta.
     * @return La tarjeta encontrada.
     */
    @Override
    @Cacheable(key = "#id")
    public Card findById(String id) {
        log.info("Buscando la tarjeta con id: " + id);
        return repository.findByPublicId(id).orElseThrow(
                () -> new CardDoesNotExistException("La tarjeta con id: " + id + " no existe"));
    }

    /**
     * Busca una tarjeta por el nombre del propietario.
     *
     * @param nombre Nombre del propietario de la tarjeta.
     * @return La tarjeta encontrada.
     */
    @Override
    public Card findByOwner(String nombre) {
        log.info("Buscando el producto con nombre: " + nombre);
        return repository.findByCardOwner(nombre.trim().toUpperCase()).orElseThrow(
                () -> new CardDoesNotExistException("La tarjeta con nombre: " + nombre + " no existe"));
    }

    /**
     * Valida y recupera un tipo de tarjeta por su nombre.
     *
     * @param name Nombre del tipo de tarjeta.
     * @return El tipo de tarjeta encontrado.
     */
    public CardType existing(String name) {
        return repositoryCardType.findByName(name);
    }

    /**
     * Valida y recupera una cuenta por su IBAN.
     *
     * @param iban IBAN de la cuenta.
     * @return La cuenta encontrada.
     */
    public Account existingAccount(String iban) {
        return accountRepository.findByIban(iban);
    }

    /**
     * Verifica si una cuenta no está eliminada.
     *
     * @param account La cuenta a verificar.
     * @return La cuenta si no está eliminada.
     * @throws AccountException si la cuenta está eliminada.
     */
    public Account notDeleteAccount(Account account) {
        if (account.isDeleted()) {
            throw new AccountException("Cuenta borrada");
        }
        return account;
    }

    /**
     * Verifica si un tipo de tarjeta no está eliminado.
     *
     * @param cardType El tipo de tarjeta a verificar.
     * @return El tipo de tarjeta si no está eliminado.
     * @throws CardException si el tipo de tarjeta está eliminado.
     */
    public CardType notDelete(CardType cardType) {
        if (cardType.getIsDeleted()) {
            throw new CardException("El tipo de tarjeta esta borrado");
        }
        return cardType;
    }

    /**
     * Valida un tipo de tarjeta por su ID.
     *
     * @param id ID del tipo de tarjeta.
     * @return El tipo de tarjeta validado.
     */
    public CardType validation(String id) {
        CardType c = existing(id);
        notDelete(c);
        return c;
    }

    /**
     * Valida una cuenta por su IBAN.
     *
     * @param iban IBAN de la cuenta.
     * @return La cuenta validada.
     */
    public Account validationAccount(String iban) {
        Account c = existingAccount(iban);
        notDeleteAccount(c);
        return c;
    }

    /**
     * Verifica si un IBAN ya está en uso por otra tarjeta.
     *
     * @param card La tarjeta a verificar.
     * @throws CardIbanInUse si el IBAN ya está en uso.
     */
    public void isIbanInUse(Card card) {
        String iban = card.getAccount().getIban();
        if (repository.existsByAccount_Iban(iban)) {
            throw new CardIbanInUse("El IBAN: " + iban + " esta ya en uso.");
        }
    }

    /**
     * Guarda una nueva tarjeta.
     *
     * @param card Datos de la tarjeta a guardar.
     * @return La tarjeta guardada.
     */
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

    /**
     * Elimina una tarjeta por su ID.
     *
     * @param id ID de la tarjeta a eliminar.
     * @return La tarjeta eliminada.
     */
    @Override
    @CacheEvict(key = "#id")
    public Card deleteById(String id) {
        log.info("Eliminando la tarjeta con id: " + id);
        Card result = findById(id);
        result.setIsDeleted(true);
        result.setLastUpdate(LocalDateTime.now());
        return repository.save(result);
    }

    /**
     * Actualiza una tarjeta por su ID.
     *
     * @param id         ID de la tarjeta a actualizar.
     * @param updateCard Datos de la tarjeta a actualizar.
     * @return La tarjeta actualizada.
     */
    @Override
    @CachePut(key = "#id")
    public Card updateById(String id, UpdateRequestCard updateCard) {
        log.info("Actualizando la tarjeta con id: " + id);
        Card existingCard = findById(id);
        Card updatedCard = CardMapper.toCard(updateCard, existingCard);
        return repository.save(updatedCard);
    }

    /**
     * Resetea los montos gastados de las tarjetas en lotes.
     */
    @Scheduled(cron = "0 0 0 * * ?")
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

    /**
     * Resetea los montos gastados para una lista de tarjetas.
     *
     * @param cards La lista de tarjetas a resetear.
     */
    void resetSpentAmountsForCards(List<Card> cards) {
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