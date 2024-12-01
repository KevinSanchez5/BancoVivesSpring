package vives.bancovives.rest.products.cardtype.repositories;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import vives.bancovives.rest.products.cardtype.model.CardType;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CardTypeRepositoryTest {

    @Autowired
    private CardTypeRepository cardTypeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static CardType cardType;
    private static String publicId;
    private static UUID id;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeEach
    void setUp() {
        entityManager.merge(cardType);
        entityManager.flush();
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        publicId = IdGenerator.generateId();
        id = UUID.randomUUID();
        cardType = CardType.builder()
                .id(id)
                .publicId(publicId)
                .name("Test Account Type")
                .description("Test description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void findById() {
        // Arrange
        cardTypeRepository.save(cardType);

        // Act
        Optional<CardType> result = cardTypeRepository.findById(id);

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get(), cardType)
        );
    }

    @Test
    void findById_ProductDoesNotExist() {
        // Arrange
        Optional<CardType> result = cardTypeRepository.findById(UUID.randomUUID());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByPublicId() {
        // Arrange
        cardTypeRepository.save(cardType);

        // Act
        Optional<CardType> result = cardTypeRepository.findByPublicId(publicId);

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get(), cardType)
        );
    }

    @Test
    void findByPublicId_ProductDoesNotExist() {
        // Arrange
        Optional<CardType> result = cardTypeRepository.findByPublicId("non-existent-id");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void save() {
        // Act
        CardType savedAccountType = cardTypeRepository.save(cardType);

        // Assert
        assertAll(
                () -> assertNotNull(savedAccountType.getId()),
                () -> assertEquals(savedAccountType.getPublicId(), publicId),
                () -> assertNotNull(savedAccountType.getCreatedAt()),
                () -> assertNotNull(savedAccountType.getUpdatedAt()),
                () -> assertEquals(savedAccountType.getName(), cardType.getName()),
                () -> assertEquals(savedAccountType.getDescription(), cardType.getDescription())
        );
    }

    @Test
    void deleteById() {
        // Arrange
        cardTypeRepository.save(cardType);

        // Act
        cardTypeRepository.deleteById(id);

        // Assert
        assertFalse(cardTypeRepository.findById(id).isPresent());
    }

    @Test
    void findByName() {
        // Arrange
        cardTypeRepository.save(cardType);

        // Act
        Optional<CardType> result = cardTypeRepository.findByName(cardType.getName());

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get(), cardType)
        );
    }

    @Test
    void findByName_ProductDoesNotExist() {
        // Arrange
        Optional<CardType> result = cardTypeRepository.findByName("non-existent-name");

        // Assert
        assertFalse(result.isPresent());
    }

}