package vives.bancovives.rest.products.accounttype.mappers;

import org.junit.jupiter.api.Test;
import vives.bancovives.rest.products.accounttype.dto.input.NewAccountType;
import vives.bancovives.rest.products.accounttype.dto.input.UpdatedAccountType;
import vives.bancovives.rest.products.accounttype.dto.output.OutputAccountType;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountTypeMapperTest {
    @Test
    void toOutputAccountType() {
        String publicId = IdGenerator.generateId();

        // Arrange
        AccountType accountType = AccountType.builder()
                .id(UUID.randomUUID())
                .publicId(publicId)
                .interest(0.0)
                .name("Product 1")
                .description("This is a test product")
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .build();

        // Act
        OutputAccountType outputAccountType = AccountTypeMapper.toOutputAccountType(accountType);

        // Assert
        assertEquals(publicId, outputAccountType.getId());
        assertEquals(accountType.getName(), outputAccountType.getName());
        assertEquals(accountType.getInterest(), outputAccountType.getInterest());
        assertEquals(accountType.getDescription(), outputAccountType.getDescription());
        assertEquals(accountType.getCreatedAt().toString(), outputAccountType.getCreatedAt());
        assertEquals(accountType.getUpdatedAt().toString(), outputAccountType.getUpdatedAt());
        assertEquals(accountType.getIsDeleted(), outputAccountType.getIsDeleted());
    }

    @Test
    void toAccountType() {
        // Arrange
        NewAccountType input = NewAccountType.builder()
                .name("Product 1")
                .description("This is a test product")
                .interest(0.0)
                .build();

        // Act
        AccountType product = AccountTypeMapper.toAccountType(input);

        // Assert
        assertEquals("PRODUCT 1", product.getName());
        assertEquals(input.getInterest(), product.getInterest());
        assertEquals(input.getDescription(), product.getDescription());
    }

    @Test
    void updateProductFromInput() {
        // Arrange
        AccountType existingObject = AccountType.builder()
                .id(UUID.randomUUID())
                .name("Old Product")
                .interest(0.0)
                .description("Old Description")
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .build();

        UpdatedAccountType input = UpdatedAccountType.builder()
                .name("Updated Product")
                .description("Updated Description")
                .interest(0.0)
                .build();

        // Act
        AccountTypeMapper.updateAccountTypeFromInput(existingObject, input);

        // Assert
        assertEquals("UPDATED PRODUCT", existingObject.getName());
        assertEquals(existingObject.getDescription(), existingObject.getDescription());
        assertNotNull(existingObject.getUpdatedAt());
    }

    @Test
    void updateProductFromInput_ShouldOnlyUpdateNonNullFields() {
        // Arrange
        AccountType existingObject = AccountType.builder()
                .id(UUID.randomUUID())
                .interest(0.0)
                .name("Old Product")
                .description("Old Description")
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .build();

        UpdatedAccountType input = UpdatedAccountType.builder()
                .name("Updated Product")
                .interest(0.0)
                .build();

        // Act
        AccountTypeMapper.updateAccountTypeFromInput(existingObject, input);

        // Assert
        assertEquals("UPDATED PRODUCT", existingObject.getName());
        assertEquals(existingObject.getDescription(), "Old Description");
        assertEquals(existingObject.getInterest(), 0.0);
        assertNotNull(existingObject.getUpdatedAt());
    }
}