package vives.bancovives.rest.cards.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.dto.output.OutputCard;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.cardtype.model.CardType;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardMapperTest {

    private CardType cardType;
    private Account account;
    private AccountType accountType;
    private Client client;

    @BeforeEach
    void setUp() {
        cardType = new CardType();
        accountType = AccountType.builder()
                .id(UUID.randomUUID())
                .publicId("asdasdasd")
                .interest(0.0)
                .name("Product 1")
                .description("This is a test product")
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .build();

        client = new Client();
        client.setPublicId("client-public-id");

        account = Account.builder()
                .client(client)
                .accountType(accountType)
                .build();
    }

    @Test
    void toCard_InputCard() {
        // Arrange
        InputCard inputCard = InputCard.builder()
                .cardOwner("JOHN DOE")
                .pin("1234")
                .dailyLimit(1000.0)
                .weeklyLimit(5000.0)
                .monthlyLimit(20000.0)
                .build();

        // Act
        Card card = CardMapper.toCard(inputCard, cardType, account);

        // Assert
        assertEquals("JOHN DOE", card.getCardOwner());
        assertEquals("1234", card.getPin());
        assertEquals(1000.0, card.getDailyLimit());
    }

    @Test
    void toCard_UpdateRequestCard() {
        // Arrange
        Card existingCard = Card.builder()
                .id(UUID.randomUUID())
                .publicId("public-id")
                .cardOwner("JOHN DOE")
                .pin("1234")
                .dailyLimit(1000.0)
                .weeklyLimit(5000.0)
                .monthlyLimit(20000.0)
                .cvv(123)
                .cardType(cardType)
                .cardNumber("1234567890123456")
                .isInactive(false)
                .isDeleted(false)
                .expirationDate("12/25")
                .creationDate(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .build();

        UpdateRequestCard request = UpdateRequestCard.builder()
                .pin("4321")
                .dailyLimit(2000.0)
                .build();

        // Act
        Card updatedCard = CardMapper.toCard(request, existingCard);

        // Assert
        assertEquals("4321", updatedCard.getPin());
        assertEquals(2000.0, updatedCard.getDailyLimit());
    }

    @Test
    void toOutputCard() {
        // Arrange
        Card card = Card.builder()
                .publicId("public-id")
                .cardOwner("JOHN DOE")
                .cardNumber("1234567890123456")
                .expirationDate("12/25")
                .cvv(123)
                .pin("1234")
                .cardType(cardType)
                .account(account)
                .dailyLimit(1000.0)
                .weeklyLimit(5000.0)
                .monthlyLimit(20000.0)
                .isInactive(false)
                .isDeleted(false)
                .creationDate(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .build();

        // Act
        OutputCard outputCard = CardMapper.toOutputCard(card);

        // Assert
        assertEquals("public-id", outputCard.getId());
        assertEquals("1234567890123456", outputCard.getCardNumber());
        assertEquals("JOHN DOE", outputCard.getCardOwner());
        assertEquals("12/25", outputCard.getExpirationDate());
    }
}