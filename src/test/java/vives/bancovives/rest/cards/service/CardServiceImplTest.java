package vives.bancovives.rest.cards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vives.bancovives.rest.accounts.exception.AccountException;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.service.AccountService;
import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.exceptions.CardDoesNotExistException;
import vives.bancovives.rest.cards.exceptions.CardException;
import vives.bancovives.rest.cards.exceptions.CardIbanInUse;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.repository.CardsRepository;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.cardtype.model.CardType;
import vives.bancovives.rest.products.cardtype.service.CardTypeService;
import vives.bancovives.utils.IdGenerator;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardsRepository cardsRepository;

    @Mock
    private CardTypeService cardTypeService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card card;
    private Account account;
    private CardType cardType;
    private Client client;
    private AccountType accountType;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(UUID.randomUUID())
                .publicId("client-public-id")
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .build();

        accountType = AccountType.builder()
                .id(UUID.randomUUID())
                .publicId(IdGenerator.generateId())
                .name("SOMETHING")
                .description("idk")
                .interest(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        account = Account.builder()
                .id(UUID.randomUUID())
                .publicId("account-public-id")
                .iban("ES1234567890123456789012")
                .balance(1000.0)
                .password("securepassword")
                .accountType(accountType)
                .client(client)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        cardType = CardType.builder()
                .id(UUID.randomUUID())
                .name("SOMETHING")
                .description("idk")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        card = Card.builder()
                .id(UUID.randomUUID())
                .publicId(IdGenerator.generateId())
                .cardOwner("John Doe")
                .cardNumber("1234567890123456")
                .expirationDate("12/25")
                .cvv(123)
                .pin("1234")
                .dailyLimit(1000.0)
                .weeklyLimit(5000.0)
                .monthlyLimit(20000.0)
                .isInactive(false)
                .isDeleted(false)
                .creationDate(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .account(account)
                .cardType(cardType)
                .build();
    }

    @Test
    void findAll_ShouldReturnPageOfCards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(Collections.singletonList(card), pageable, 1);

        when(cardsRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Card> result = cardService.findAll(
                Optional.empty(),
                Optional.of("John Doe"),
                Optional.of(false),
                Optional.of(false),
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(card, result.getContent().get(0));
    }

    @Test
    void saveCard_Successful() {
        // Arrange
        InputCard inputCard = InputCard.builder()
                .cardOwner("John Doe")
                .cardTypeName(cardType.getName())
                .account(account.getIban())
                .pin("1234")
                .build();

        when(accountService.findByIban(inputCard.getAccount())).thenReturn(account);
        when(cardTypeService.findByName(inputCard.getCardTypeName())).thenReturn(cardType);
        when(cardsRepository.save(any(Card.class))).thenReturn(card);

        // Act
        Card result = cardService.save(inputCard);

        // Assert
        assertNotNull(result);
        assertEquals(card.getCardOwner(), result.getCardOwner());
        verify(cardsRepository).save(any(Card.class));
    }

    @Test
    void saveCard_Failure_CardNotSaved() {
        // Arrange
        InputCard inputCard = InputCard.builder()
                .cardOwner("John Doe")
                .cardTypeName(cardType.getName())
                .account(account.getIban())
                .build();

        when(accountService.findByIban(inputCard.getAccount())).thenReturn(account);
        when(cardTypeService.findByName(inputCard.getCardTypeName())).thenReturn(cardType);
        when(cardsRepository.save(any(Card.class))).thenThrow(new RuntimeException("Error al guardar la tarjeta"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cardService.save(inputCard),
                "Se esperaba una excepción al intentar guardar la tarjeta"
        );

        assertEquals("Error al guardar la tarjeta", exception.getMessage());
        verify(cardsRepository).save(any(Card.class));
    }

    @Test
    void saveCard_Failure_CardAlreadyExists() {
        // Arrange
        InputCard inputCard = InputCard.builder()
                .cardOwner("John Doe")
                .cardTypeName(cardType.getName())
                .account(account.getIban())
                .build();

        when(accountService.findByIban(inputCard.getAccount())).thenReturn(account);
        when(cardTypeService.findByName(inputCard.getCardTypeName())).thenReturn(cardType);
        when(cardsRepository.existsByAccount_Iban(inputCard.getAccount())).thenReturn(true);

        // Act & Assert
        assertThrows(
                CardIbanInUse.class,
                () -> cardService.save(inputCard),
                "Se esperaba una excepción al intentar guardar la tarjeta"
        );

        verify(cardsRepository, never()).save(any(Card.class));
    }

    @Test
    void existing_ShouldReturnCardType() {
        when(cardTypeService.findByName("SOMETHING")).thenReturn(cardType);

        CardType result = cardService.existing("SOMETHING");

        assertEquals(cardType, result);
        verify(cardTypeService).findByName("SOMETHING");
    }

    @Test
    void existingAccount_ShouldReturnAccount() {
        when(accountService.findByIban("ES1234567890123456789012")).thenReturn(account);

        Account result = cardService.existingAccount("ES1234567890123456789012");

        assertEquals(account, result);
        verify(accountService).findByIban("ES1234567890123456789012");
    }

    @Test
    void notDeleteAccount_ShouldReturnAccount() {
        Account result = cardService.notDeleteAccount(account);

        assertEquals(account, result);
    }

    @Test
    void notDeleteAccount_ShouldThrowAccountException() {
        account.setDeleted(true);

        assertThrows(AccountException.class, () -> cardService.notDeleteAccount(account));
    }

    @Test
    void notDelete_ShouldReturnCardType() {
        CardType result = cardService.notDelete(cardType);

        assertEquals(cardType, result);
    }

    @Test
    void notDelete_ShouldThrowCardException() {
        cardType.setIsDeleted(true);

        assertThrows(CardException.class, () -> cardService.notDelete(cardType));
    }

    @Test
    void validation_ShouldReturnCardType() {
        when(cardTypeService.findByName("SOMETHING")).thenReturn(cardType);

        CardType result = cardService.validation("SOMETHING");

        assertEquals(cardType, result);
        verify(cardTypeService).findByName("SOMETHING");
    }

    @Test
    void validationAccount_ShouldReturnAccount() {
        when(accountService.findByIban("ES1234567890123456789012")).thenReturn(account);

        Account result = cardService.validationAccount("ES1234567890123456789012");

        assertEquals(account, result);
        verify(accountService).findByIban("ES1234567890123456789012");
    }

    @Test
    void isIbanInUse_ShouldThrowCardIbanInUse() {
        when(cardsRepository.existsByAccount_Iban("ES1234567890123456789012")).thenReturn(true);

        assertThrows(CardIbanInUse.class, () -> cardService.isIbanInUse(card));
    }

    @Test
    void isIbanInUse_ShouldNotThrowException() {
        when(cardsRepository.existsByAccount_Iban("ES1234567890123456789012")).thenReturn(false);

        assertDoesNotThrow(() -> cardService.isIbanInUse(card));
    }

    @Test
    void findByOwner_ShouldReturnCard() {
        when(cardsRepository.findByCardOwner("JOHN DOE")).thenReturn(Optional.of(card));

        Card result = cardService.findByOwner("John Doe");

        assertEquals(card, result);
        verify(cardsRepository).findByCardOwner("JOHN DOE");
    }

    @Test
    void findByOwner_ShouldThrowCardDoesNotExistException() {
        when(cardsRepository.findByCardOwner("JOHN DOE")).thenReturn(Optional.empty());

        assertThrows(CardDoesNotExistException.class, () -> cardService.findByOwner("John Doe"));
    }

    @Test
    void deleteById_ShouldMarkCardAsDeleted() {
        when(cardsRepository.findByPublicId("card-public-id")).thenReturn(Optional.of(card));
        when(cardsRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.deleteById("card-public-id");

        assertTrue(result.getIsDeleted());
        assertNotNull(result.getLastUpdate());
        verify(cardsRepository).findByPublicId("card-public-id");
        verify(cardsRepository).save(card);
    }

    @Test
    void deleteById_ShouldThrowCardDoesNotExistException() {
        when(cardsRepository.findByPublicId("card-public-id")).thenReturn(Optional.empty());

        assertThrows(CardDoesNotExistException.class, () -> cardService.deleteById("card-public-id"));
        verify(cardsRepository, never()).save(any(Card.class));
    }

/*    @Test
    void resetSpentAmountsInBatches_ShouldResetSpentAmounts() {
        Pageable pageable = PageRequest.of(0, 200);
        List<Card> cards = Collections.singletonList(card);
        Page<Card> page = new PageImpl<>(cards, pageable, 1);

        when(cardsRepository.findAll(pageable)).thenReturn(page);
        when(cardsRepository.findAll(page.nextPageable())).thenReturn(Page.empty());

        cardService.resetSpentAmountsInBatches();

        verify(cardsRepository, times(2)).findAll(any(Pageable.class));
        verify(cardsRepository).saveAll(cards);

        assertEquals(0, card.getSpentToday());
        assertEquals(0, card.getSpentThisWeek());
        assertEquals(0, card.getSpentThisMonth());
    }*/

    @Test
    void resetSpentAmountsForCards_ShouldResetSpentAmounts() {
        List<Card> cards = Collections.singletonList(card);
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();

        cardService.resetSpentAmountsForCards(cards);

        assertEquals(0, card.getSpentToday());
        if (now.getDayOfWeek() == firstDayOfWeek) {
            assertEquals(0, card.getSpentThisWeek());
        }
        if (now.getDayOfMonth() == 1) {
            assertEquals(0, card.getSpentThisMonth());
        }
    }

    @Test
    void updateById_ShouldUpdateCardPin() {
        // Arrange
        String cardId = card.getId().toString();
        UpdateRequestCard updateRequestCard = UpdateRequestCard.builder()
                .pin("4321")
                .build();

        Card updatedCard = Card.builder()
                .id(card.getId())
                .cardOwner(card.getCardOwner())
                .pin("4321")
                .isInactive(card.getIsInactive())
                .isDeleted(card.getIsDeleted())
                .creationDate(card.getCreationDate())
                .build();

        when(cardsRepository.findByPublicId(cardId)).thenReturn(Optional.of(card));
        when(cardsRepository.save(any(Card.class))).thenReturn(updatedCard);

        // Act
        Card result = cardService.updateById(cardId, updateRequestCard);

        // Assert
        assertEquals("4321", result.getPin());
        verify(cardsRepository).findByPublicId(cardId);
        verify(cardsRepository).save(any(Card.class));
    }
}