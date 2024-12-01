package vives.bancovives.rest.products.accounttype.repositories;

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
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.utils.IdGenerator;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountTypeRepositoryTest {

    @Autowired
    private AccountTypeRepository accountTypeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static AccountType accountType;
    private static String publicId;
    private static UUID id;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeEach
    void setUp() {
        entityManager.merge(accountType);
        entityManager.flush();
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        publicId = IdGenerator.generateId();
        id = UUID.randomUUID();
        accountType = AccountType.builder()
                .id(id)
                .publicId(publicId)
                .name("Test Account Type")
                .description("Test description")
                .interest(0.05)
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
        accountTypeRepository.save(accountType);

        // Act
        Optional<AccountType> result = accountTypeRepository.findById(id);

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get(), accountType)
        );
    }

    @Test
    void findById_ProductDoesNotExist() {
        // Arrange
        Optional<AccountType> result = accountTypeRepository.findById(UUID.randomUUID());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByPublicId() {
        // Arrange
        accountTypeRepository.save(accountType);

        // Act
        Optional<AccountType> result = accountTypeRepository.findByPublicId(publicId);

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get(), accountType)
        );
    }

    @Test
    void findByPublicId_ProductDoesNotExist() {
        // Arrange
        Optional<AccountType> result = accountTypeRepository.findByPublicId("non-existent-id");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void save() {
        // Act
        AccountType savedAccountType = accountTypeRepository.save(accountType);

        // Assert
        assertAll(
                () -> assertNotNull(savedAccountType.getId()),
                () -> assertEquals(savedAccountType.getPublicId(), publicId),
                () -> assertNotNull(savedAccountType.getCreatedAt()),
                () -> assertNotNull(savedAccountType.getUpdatedAt()),
                () -> assertEquals(savedAccountType.getName(), accountType.getName()),
                () -> assertEquals(savedAccountType.getDescription(), accountType.getDescription()),
                () -> assertEquals(savedAccountType.getInterest(), accountType.getInterest())
        );
    }

    @Test
    void deleteById() {
        // Arrange
        accountTypeRepository.save(accountType);

        // Act
        accountTypeRepository.deleteById(id);

        // Assert
        assertFalse(accountTypeRepository.findById(id).isPresent());
    }

    @Test
    void findByName() {
        // Arrange
        accountTypeRepository.save(accountType);

        // Act
        Optional<AccountType> result = accountTypeRepository.findByName(accountType.getName());

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get(), accountType)
        );
    }

    @Test
    void findByName_ProductDoesNotExist() {
        // Arrange
        Optional<AccountType> result = accountTypeRepository.findByName("non-existent-name");

        // Assert
        assertFalse(result.isPresent());
    }

}