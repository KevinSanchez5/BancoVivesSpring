package vives.bancovives.rest.products.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import vives.bancovives.rest.products.accounttype.dto.input.NewAccountType;
import vives.bancovives.rest.products.accounttype.dto.input.UpdatedAccountType;
import vives.bancovives.rest.products.accounttype.dto.output.OutputAccountType;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.accounttype.service.AccountTypeService;
import vives.bancovives.rest.products.cardtype.dto.input.NewCardType;
import vives.bancovives.rest.products.cardtype.dto.input.UpdatedCardType;
import vives.bancovives.rest.products.cardtype.dto.output.OutputCardType;
import vives.bancovives.rest.products.cardtype.model.CardType;
import vives.bancovives.rest.products.cardtype.service.CardTypeService;
import vives.bancovives.utils.IdGenerator;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountTypeService accountTypeService;

    @MockBean
    private CardTypeService cardTypeService;

    @MockBean
    private PaginationLinksUtils paginationLinksUtils;

    @Autowired
    public ProductControllerTest(
            AccountTypeService accountTypeService,
            CardTypeService cardTypeService,
            PaginationLinksUtils paginationLinksUtils
    ) {
        this.cardTypeService = cardTypeService;
        this.accountTypeService = accountTypeService;
        mapper.registerModule(new JavaTimeModule());
    }

    private final ObjectMapper mapper = new ObjectMapper();

    // IDs
    private String publicId = IdGenerator.generateId();
    private UUID id = UUID.randomUUID();

    // AccountType
    private OutputAccountType outputAccountType;
    private AccountType accountType;

    // CardType
    private CardType cardType;
    private OutputCardType outputCardType;

    @BeforeEach
    void setUp() {
        // AccountType
        accountType = AccountType.builder()
                .id(id)
                .publicId(publicId)
                .name("SOMETHING")
                .description("idk")
                .interest(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        // OutputAccountType
        outputAccountType = OutputAccountType.builder()
                .id(publicId)
                .name("SOMETHING")
                .description("idk")
                .interest(0.0)
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .isDeleted(false)
                .build();

        // CardType
        cardType = CardType.builder()
               .id(UUID.randomUUID())
               .publicId(publicId)
               .name("SOMETHING")
               .description("idk")
               .createdAt(LocalDateTime.now())
               .updatedAt(LocalDateTime.now())
               .isDeleted(false)
               .build();

        // OutputCardType
        outputCardType = OutputCardType.builder()
               .id(publicId)
               .name("SOMETHING")
               .description("idk")
               .createdAt(LocalDateTime.now().toString())
               .updatedAt(LocalDateTime.now().toString())
               .isDeleted(false)
               .build();
    }

    @Test
    void getAllAccountTypes() throws Exception {
        // Arrange
        var list = List.of(accountType);
        Page<AccountType> page = new PageImpl<>(list);
        when(accountTypeService.findAll(
                any(Optional.class),
                any(Optional.class),
                any(Optional.class),
                any(Pageable.class))
        ).thenReturn(page);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        get("/v1/products/accounts")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        PageResponse<OutputAccountType> res = mapper.readValue(response.getContentAsString(), new TypeReference<>() {
        });

        // Assert
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(1, res.content().size())
        );

        // Verify
        verify(accountTypeService, times(1)).findAll(
                any(Optional.class),
                any(Optional.class),
                any(Optional.class),
                any(Pageable.class)
        );
    }

    @Test
    void getAccountTypeById() throws Exception {
        // Arrange
        when(accountTypeService.findById(publicId)).thenReturn(accountType);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                get("/v1/products/accounts/" + publicId)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputAccountType responseBody = mapper.readValue(response.getContentAsString(), OutputAccountType.class);
        assertAll(
                () -> assertEquals(outputAccountType.getId(), responseBody.getId()),
                () -> assertEquals(outputAccountType.getName(), responseBody.getName()),
                () -> assertEquals(outputAccountType.getInterest(), responseBody.getInterest()),
                () -> assertEquals(outputAccountType.getDescription(), responseBody.getDescription())
        );
    }

    @Test
    void getAccountTypeByName() throws Exception {
        // Arrange
        when(accountTypeService.findByName(accountType.getName())).thenReturn(accountType);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                get("/v1/products/accounts/name/" + accountType.getName())
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputAccountType responseBody = mapper.readValue(response.getContentAsString(), OutputAccountType.class);
        assertAll(
                () -> assertEquals(outputAccountType.getId(), responseBody.getId()),
                () -> assertEquals(outputAccountType.getName(), responseBody.getName()),
                () -> assertEquals(outputAccountType.getDescription(), responseBody.getDescription()),
                () -> assertEquals(outputAccountType.getInterest(), responseBody.getInterest())
        );
    }

    @Test
    void createAccountType() throws Exception {
        // Arrange
        NewAccountType newAccountType = NewAccountType.builder()
                .name("SOMETHING")
                .description("idk")
                .interest(0.0)
                .build();
        when(accountTypeService.save(any(NewAccountType.class))).thenReturn(accountType);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                post("/v1/products/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newAccountType))
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputAccountType responseBody = mapper.readValue(response.getContentAsString(), OutputAccountType.class);
        assertAll(
                () -> assertEquals(outputAccountType.getId(), responseBody.getId()),
                () -> assertEquals(outputAccountType.getName(), responseBody.getName()),
                () -> assertEquals(outputAccountType.getDescription(), responseBody.getDescription()),
                () -> assertEquals(outputAccountType.getInterest(), responseBody.getInterest())
        );
    }

    @Test
    void updateAccountType() throws Exception {
        // Arrange
        UpdatedAccountType input = UpdatedAccountType.builder()
                .name("SOMETHING2")
                .description("idk2")
                .interest(0.0)
                .build();

        AccountType serviceResponse = AccountType.builder()
                .id(id)
                .publicId(publicId)
                .name("SOMETHING2")
                .description("idk2")
                .interest(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        OutputAccountType output = OutputAccountType.builder()
                .id(publicId)
                .name("SOMETHING2")
                .description("idk2")
                .interest(0.0)
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .isDeleted(false)
                .build();

        when(accountTypeService.update(publicId, input)).thenReturn(serviceResponse);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                put("/v1/products/accounts/" + publicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input))
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputAccountType responseBody = mapper.readValue(response.getContentAsString(), OutputAccountType.class);
        assertAll(
                () -> assertEquals(output.getId(), responseBody.getId()),
                () -> assertEquals(output.getName(), responseBody.getName()),
                () -> assertEquals(output.getDescription(), responseBody.getDescription()),
                () -> assertEquals(output.getInterest(), responseBody.getInterest())
        );
    }

    @Test
    void deleteAccountType() throws Exception {
        // Arrange
        AccountType serviceResponse = AccountType.builder()
               .id(id)
               .publicId(publicId)
               .name("SOMETHING")
               .description("idk")
               .interest(0.0)
               .createdAt(LocalDateTime.now())
               .updatedAt(LocalDateTime.now())
               .isDeleted(true)
               .build();

        OutputAccountType output = OutputAccountType.builder()
               .id(publicId)
               .name("SOMETHING")
               .description("idk")
               .interest(0.0)
               .createdAt(LocalDateTime.now().toString())
               .updatedAt(LocalDateTime.now().toString())
               .isDeleted(true)
               .build();

        when(accountTypeService.delete(publicId)).thenReturn(serviceResponse);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                delete("/v1/products/accounts/" + publicId)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputAccountType responseBody = mapper.readValue(response.getContentAsString(), OutputAccountType.class);
        assertAll(
                () -> assertEquals(output.getId(), responseBody.getId()),
                () -> assertEquals(output.getName(), responseBody.getName()),
                () -> assertEquals(output.getDescription(), responseBody.getDescription()),
                () -> assertEquals(output.getInterest(), responseBody.getInterest())
        );
    }

    @Test
    void getAllCardTypes() throws Exception {
        var list = List.of(cardType);
        Page<CardType> page = new PageImpl<>(list);

        // Arrange
        when(cardTypeService.findAll(
                any(Optional.class),
                any(Optional.class),
                any(Pageable.class))
        ).thenReturn(page);

        // Consulto el endpoint
        MockHttpServletResponse response = mockMvc.perform(
                        get("/v1/products/cards")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        PageResponse<OutputAccountType> res = mapper.readValue(response.getContentAsString(), new TypeReference<>() {
        });

        // Assert
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(1, res.content().size())
        );

        // Verify
        verify(cardTypeService, times(1)).findAll(
                any(Optional.class),
                any(Optional.class),
                any(Pageable.class)
        );
    }

    @Test
    void getCardTypeById() throws Exception {
        // Arrange
        when(cardTypeService.findById(publicId)).thenReturn(cardType);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                get("/v1/products/cards/" + publicId)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputCardType responseBody = mapper.readValue(response.getContentAsString(), OutputCardType.class);
        assertAll(
                () -> assertEquals(outputCardType.getId(), responseBody.getId()),
                () -> assertEquals(outputCardType.getName(), responseBody.getName()),
                () -> assertEquals(outputCardType.getDescription(), responseBody.getDescription())
        );
    }

    @Test
    void getCardTypeByName() throws Exception {
        // Arrange
        when(cardTypeService.findByName(cardType.getName())).thenReturn(cardType);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                get("/v1/products/cards/name/" + cardType.getName())
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputCardType responseBody = mapper.readValue(response.getContentAsString(), OutputCardType.class);
        assertAll(
                () -> assertEquals(outputCardType.getId(), responseBody.getId()),
                () -> assertEquals(outputCardType.getName(), responseBody.getName()),
                () -> assertEquals(outputCardType.getDescription(), responseBody.getDescription())
        );
    }

    @Test
    void createCardType() throws Exception {
        // Arrange
        NewCardType input = NewCardType.builder()
                .name("SOMETHING")
                .description("idk")
                .build();
        when(cardTypeService.save(input)).thenReturn(cardType);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                post("/v1/products/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(outputCardType))
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputCardType responseBody = mapper.readValue(response.getContentAsString(), OutputCardType.class);
        assertAll(
                () -> assertEquals(outputCardType.getId(), responseBody.getId()),
                () -> assertEquals(outputCardType.getName(), responseBody.getName()),
                () -> assertEquals(outputCardType.getDescription(), responseBody.getDescription())
        );
    }

    @Test
    void updateCardType() throws Exception {
        // Arrange
        UpdatedCardType input = UpdatedCardType.builder()
                .name("SOMETHING2")
                .description("idk2")
                .build();

        CardType serviceResponse = CardType.builder()
                .id(id)
                .publicId(publicId)
                .name("SOMETHING2")
                .description("idk2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        OutputCardType output = OutputCardType.builder()
                .id(publicId)
                .name("SOMETHING2")
                .description("idk2")
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .isDeleted(false)
                .build();
        when(cardTypeService.update(publicId, input)).thenReturn(serviceResponse);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                put("/v1/products/cards/" + publicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input))
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputCardType responseBody = mapper.readValue(response.getContentAsString(), OutputCardType.class);
        assertAll(
                () -> assertEquals(output.getId(), responseBody.getId()),
                () -> assertEquals(output.getName(), responseBody.getName()),
                () -> assertEquals(output.getDescription(), responseBody.getDescription())
        );
    }

    @Test
    void deleteCardType() throws Exception {
        // Arrange
        CardType serviceResponse = CardType.builder()
                .id(id)
                .publicId(publicId)
                .name("SOMETHING")
                .description("idk")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(true)
                .build();

        when(cardTypeService.delete(publicId)).thenReturn(serviceResponse);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                delete("/v1/products/cards/" + publicId)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputCardType responseBody = mapper.readValue(response.getContentAsString(), OutputCardType.class);
        assertAll(
                () -> assertEquals(outputAccountType.getId(), responseBody.getId()),
                () -> assertEquals(outputAccountType.getName(), responseBody.getName()),
                () -> assertEquals(outputAccountType.getDescription(), responseBody.getDescription()),
                () -> assertTrue(responseBody.getIsDeleted())
        );
    }
}