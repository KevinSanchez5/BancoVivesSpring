package vives.bancovives.rest.products.cardtype.mappers;

import org.junit.jupiter.api.Test;
import vives.bancovives.rest.products.cardtype.dto.input.NewCardType;
import vives.bancovives.rest.products.cardtype.dto.input.UpdatedCardType;
import vives.bancovives.rest.products.cardtype.dto.output.OutputCardType;
import vives.bancovives.rest.products.cardtype.model.CardType;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardTypeMapperTest {

    @Test
    void toOutputCardType() {
        String publicId = IdGenerator.generateId();

        // Arrange
        CardType cardType = CardType.builder()
                .id(UUID.randomUUID())
                .publicId(publicId)
                .name("Product 1")
                .description("This is a test product")
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .build();

        // Act
        OutputCardType outputProduct = CardTypeMapper.toOutputCardType(cardType);

        // Assert
        assertEquals(publicId, outputProduct.getId());
        assertEquals(cardType.getName(), outputProduct.getName());
        assertEquals(cardType.getDescription(), outputProduct.getDescription());
        assertEquals(cardType.getCreatedAt().toString(), outputProduct.getCreatedAt());
        assertEquals(cardType.getUpdatedAt().toString(), outputProduct.getUpdatedAt());
        assertEquals(cardType.getIsDeleted(), outputProduct.getIsDeleted());
    }

    @Test
    void toCardType() {
        // Arrange
        NewCardType input = NewCardType.builder()
                .name("Product 1")
                .description("This is a test product")
                .build();

        // Act
        CardType product = CardTypeMapper.toCardType(input);

        // Assert
        assertEquals("PRODUCT 1", product.getName());
        assertEquals(input.getDescription(), product.getDescription());
    }

    @Test
    void updateProductFromInput() {
        // Arrange
        CardType existingObject = CardType.builder()
                .id(UUID.randomUUID())
                .name("Old Product")
                .description("Old Description")
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .build();

        UpdatedCardType input = UpdatedCardType.builder()
                .name("Updated Product")
                .description("Updated Description")
                .build();

        // Act
        CardTypeMapper.updateCardTypeFromInput(existingObject, input);

        // Assert
        assertEquals("UPDATED PRODUCT", existingObject.getName());
        assertEquals(existingObject.getDescription(), existingObject.getDescription());
        assertNotNull(existingObject.getUpdatedAt());
    }

    @Test
    void updateProductFromInput_ShouldOnlyUpdateNonNullFields() {
        // Arrange
        CardType existingObject = CardType.builder()
                .id(UUID.randomUUID())
                .name("Old Product")
                .description("Old Description")
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .build();

        UpdatedCardType input = UpdatedCardType.builder()
                .name("Updated Product")
                .build();

        // Act
        CardTypeMapper.updateCardTypeFromInput(existingObject, input);

        // Assert
        assertEquals("UPDATED PRODUCT", existingObject.getName());
        assertEquals(existingObject.getDescription(), "Old Description");
        assertNotNull(existingObject.getUpdatedAt());
    }
}