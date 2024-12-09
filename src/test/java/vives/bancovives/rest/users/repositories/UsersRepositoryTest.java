package vives.bancovives.rest.users.repositories;

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
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UsersRepositoryTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static User user;
    private static String publicId;
    private static UUID id;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        publicId = IdGenerator.generateId();
        id = UUID.randomUUID();
        user = User.builder()
                .id(id)
                .publicId(publicId)
                .username("testUser")
                .password("password123")
                .roles(Set.of(Role.USER))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setUp() {
        entityManager.merge(user);
        entityManager.flush();
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
    void findByUsername() {
        // Arrange
        usersRepository.save(user);

        // Act
        Optional<User> result = usersRepository.findByUsername(user.getUsername());

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get(), user)
        );
    }

    @Test
    void findByUsername_UserDoesNotExist() {
        // Act
        Optional<User> result = usersRepository.findByUsername("nonexistentUser");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByUsernameEqualsIgnoreCase() {
        // Arrange
        usersRepository.save(user);

        // Act
        Optional<User> result = usersRepository.findByUsernameEqualsIgnoreCase(user.getUsername().toUpperCase());

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get(), user)
        );
    }

    @Test
    void findByUsernameEqualsIgnoreCase_UserDoesNotExist() {
        // Act
        Optional<User> result = usersRepository.findByUsernameEqualsIgnoreCase("NONEXISTENTUSER");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByPublicId() {
        // Arrange
        usersRepository.save(user);

        // Act
        Optional<User> result = usersRepository.findByPublicId(publicId);

        // Assert
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get(), user)
        );
    }

    @Test
    void findByPublicId_UserDoesNotExist() {
        // Act
        Optional<User> result = usersRepository.findByPublicId("non-existent-id");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void save() {
        // Act
        User savedUser = usersRepository.save(user);

        // Assert
        assertAll(
                () -> assertNotNull(savedUser.getId()),
                () -> assertEquals(savedUser.getPublicId(), publicId),
                () -> assertNotNull(savedUser.getCreatedAt()),
                () -> assertNotNull(savedUser.getUpdatedAt()),
                () -> assertEquals(savedUser.getUsername(), user.getUsername()),
                () -> assertEquals(savedUser.getPassword(), user.getPassword())
        );
    }

    @Test
    void deleteById() {
        // Arrange
        usersRepository.save(user);

        // Act
        usersRepository.deleteById(id);

        // Assert
        assertFalse(usersRepository.findById(id).isPresent());
    }
}
