package vives.bancovives.rest.products.accounttype.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vives.bancovives.rest.products.accounttype.dto.input.NewAccountType;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.accounttype.service.AccountTypeService;
import vives.bancovives.utils.IdGenerator;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountTypeStorageCSVImplTest {

    @Mock
    private AccountTypeService service;

    @InjectMocks
    private AccountTypeStorageCSVImpl storageCSV;

    private NewAccountType newAccountType;

    private File tempFile;

    @BeforeEach
    void setUp() throws Exception {
        newAccountType = NewAccountType.builder()
                .name("SOMETHING")
                .interest(0.0)
                .description("idk")
                .build();
        tempFile = File.createTempFile("test", ".csv");
    }
    @AfterEach
    void tearDown() throws IOException {
        tempFile.delete();
    }

    @Test
    void save() throws Exception {
        // Arrange
        AccountType accountType = AccountType.builder()
               .id(UUID.randomUUID())
                .publicId(IdGenerator.generateId())
               .name("SOMETHING")
               .description("idk")
               .interest(0.0)
               .createdAt(LocalDateTime.now())
               .updatedAt(LocalDateTime.now())
               .isDeleted(false)
               .build();

        // Act
        storageCSV.save(List.of(accountType), tempFile);
        List<NewAccountType> saved = storageCSV.read(tempFile).collectList().block();

        // Assert
        assertEquals(accountType.getName(), saved.getFirst().getName());
        assertEquals(accountType.getDescription(), saved.getFirst().getDescription());
        assertEquals(accountType.getInterest(), saved.getFirst().getInterest());
    }

    @Test
    void read() throws Exception {
        // Arrange
        AccountType accountType = AccountType.builder()
               .id(UUID.randomUUID())
               .publicId(IdGenerator.generateId())
               .name("SOMETHING")
               .description("idk")
               .interest(0.0)
               .createdAt(LocalDateTime.now())
               .updatedAt(LocalDateTime.now())
               .isDeleted(false)
               .build();
        storageCSV.save(List.of(accountType), tempFile);

        // Act
        List<NewAccountType> saved = storageCSV.read(tempFile).collectList().block();

        // Assert
        assertEquals(1, saved.size());
        assertEquals(accountType.getName(), saved.get(0).getName());
        assertEquals(accountType.getInterest(), saved.get(0).getInterest());
        assertEquals(accountType.getDescription(), saved.get(0).getDescription());
    }

    @Test
    void readAnEmptyFile() throws Exception {
        // Arrange
        File tempFile = File.createTempFile("test", ".csv");

        // Act
        List<NewAccountType> saved = storageCSV.read(tempFile).collectList().block();

        // Assert
        assertEquals(0, saved.size());
    }
}