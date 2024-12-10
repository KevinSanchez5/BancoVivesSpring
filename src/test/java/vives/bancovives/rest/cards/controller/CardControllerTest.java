package vives.bancovives.rest.cards.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.service.AccountService;
import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.dto.output.OutputCard;
import vives.bancovives.rest.cards.mapper.CardMapper;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.service.CardService;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.cardtype.model.CardType;
import vives.bancovives.rest.products.cardtype.service.CardTypeService;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.security.model.JwtAuthResponse;
import vives.bancovives.utils.IdGenerator;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UsersService usersService;
    @MockBean
    private PaginationLinksUtils paginationLinksUtils;
    @Mock
    private CardTypeService cardTypeService;
    @Mock
    private AccountService accountService;
    @MockBean
    private CardService cardService;

    private User user;
    private JwtAuthResponse jwtAuthResponse;
    private Card card;
    private Account account;
    private CardType cardType;
    private Client client;
    private AccountType accountType;

    private OutputCard outputCard;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .password("password")
                .build();

        jwtAuthResponse = JwtAuthResponse.builder()
                .token("token")
                .build();

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

        outputCard = CardMapper.toOutputCard(card);
    }

/*
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllCards() throws Exception {
        // Arrange
        var list = List.of(outputCard);
        Page<OutputCard> page = new PageImpl<>(list);

        when(cardService.findAll(
                any(Optional.class),
                any(Optional.class),
                any(Optional.class),
                any(Optional.class),
                any(Pageable.class))
        ).thenReturn(page);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        get("/v1/cards")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        PageResponse<OutputCard> res = mapper.readValue(response.getContentAsString(), new TypeReference<>() {
        });

        // Assert
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(1, res.content().size())
        );

        // Verify
        verify(cardService, times(1)).findAll(
                any(Optional.class),
                any(Optional.class),
                any(Optional.class),
                any(Optional.class),
                any(Pageable.class)
        );
    }*/


    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void createCard_Successful() throws Exception {
        // Arrange
        InputCard inputCard = InputCard.builder()
                .cardOwner("John Doe")
                .cardTypeName(cardType.getName())
                .account(account.getIban())
                .build();

        when(cardService.save(any(InputCard.class))).thenReturn(card);

        // Act
        mockMvc.perform(
                        post("/v1/cards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(inputCard))
                                .accept(MediaType.APPLICATION_JSON)
                )
                // Assert
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(CardMapper.toOutputCard(card))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getCardById_Successful() throws Exception {
        // Arrange
        String cardId = card.getPublicId();
        when(cardService.findById(cardId)).thenReturn(card);

        // Act
        mockMvc.perform(
                        get("/v1/cards/{id}", cardId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                // Assert
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(CardMapper.toOutputCard(card))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void getCardByName_Successful() throws Exception {
        // Arrange
        String cardOwner = card.getCardOwner();
        when(cardService.findByOwner(cardOwner)).thenReturn(card);

        // Act
        mockMvc.perform(
                        get("/v1/cards/name/{name}", cardOwner)
                                .accept(MediaType.APPLICATION_JSON)
                )
                // Assert
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(CardMapper.toOutputCard(card))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateCard() throws Exception {
        // Arrange
        String cardId = card.getPublicId();

        UpdateRequestCard input = UpdateRequestCard.builder()
                .dailyLimit(2000.0)
                .weeklyLimit(10000.0)
                .monthlyLimit(40000.0)
                .pin("567") // Valid pin value
                .isInactive(false)
                .build();

        Card serviceResponse = Card.builder()
                .id(card.getId())
                .publicId(card.getPublicId())
                .cardOwner(card.getCardOwner())
                .cardNumber(card.getCardNumber())
                .expirationDate(card.getExpirationDate())
                .cvv(card.getCvv())
                .pin(input.getPin())
                .dailyLimit(input.getDailyLimit())
                .weeklyLimit(input.getWeeklyLimit())
                .monthlyLimit(input.getMonthlyLimit())
                .isInactive(input.getIsInactive())
                .isDeleted(card.getIsDeleted())
                .creationDate(card.getCreationDate())
                .lastUpdate(LocalDateTime.now())
                .account(card.getAccount())
                .cardType(card.getCardType())
                .build();

        OutputCard output = CardMapper.toOutputCard(serviceResponse);

        when(cardService.updateById(cardId, input)).thenReturn(serviceResponse);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                put("/v1/cards/" + cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input))
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputCard responseBody = mapper.readValue(response.getContentAsString(), OutputCard.class);
        assertAll(
                () -> assertEquals(output.getCardOwner(), responseBody.getCardOwner()),
                () -> assertEquals(output.getDailyLimit(), responseBody.getDailyLimit()),
                () -> assertEquals(output.getWeeklyLimit(), responseBody.getWeeklyLimit()),
                () -> assertEquals(output.getMonthlyLimit(), responseBody.getMonthlyLimit()),
                () -> assertEquals(output.getPin(), responseBody.getPin())
        );
    }

/*    @Test
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void deleteCardById_Successful() throws Exception {
        // Arrange
        String cardId = IdGenerator.generateId();

        // Crear el objeto OutputCard que esperamos como respuesta
        OutputCard outputCard = OutputCard.builder()
                .id(cardId)
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
                .build();

        // Mockear la respuesta del servicio
        when(cardService.deleteById(cardId)).thenReturn(card);

        // Act
        mockMvc.perform(
                        delete("/v1/cards/{id}", cardId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                // Assert
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(outputCard)));
    }*/


}