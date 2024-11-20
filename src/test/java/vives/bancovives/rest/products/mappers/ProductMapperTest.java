package vives.bancovives.rest.products.mappers;

import org.junit.jupiter.api.Test;
import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.dto.output.OutputProduct;
import vives.bancovives.rest.products.model.Product;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    @Test
    void toOutputProduct() {
        // Arrange
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Product 1")
                .description("This is a test product")
                .interest(0.1)
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .productType("TEST")
                .build();

        // Act
        OutputProduct outputProduct = ProductMapper.toOutputProduct(product);

        // Assert
        assertEquals(product.getId(), outputProduct.getId());
        assertEquals(product.getName(), outputProduct.getName());
        assertEquals(product.getDescription(), outputProduct.getDescription());
        assertEquals(product.getInterest(), outputProduct.getInterest());
        assertEquals(product.getCreatedAt().toString(), outputProduct.getCreatedAt());
        assertEquals(product.getUpdatedAt().toString(), outputProduct.getUpdatedAt());
        assertEquals(product.getIsDeleted(), outputProduct.getIsDeleted());
        assertEquals(product.getProductType(), outputProduct.getProductType());
    }

    @Test
    void toProduct() {
        // Arrange
        InputProduct inputProduct = InputProduct.builder()
                .name("Product 1")
                .description("This is a test product")
                .interest(0.1)
                .productType("TEST")
                .build();

        // Act
        Product product = ProductMapper.toProduct(inputProduct);

        // Assert
        assertEquals("PRODUCT 1", product.getName());
        assertEquals(inputProduct.getDescription(), product.getDescription());
        assertEquals(inputProduct.getInterest(), product.getInterest());
    }

    @Test
    void toProduct_ShouldHandleNullInterest() {
        // Arrange
        InputProduct inputProduct = InputProduct.builder()
                .name("Test Input")
                .description("Test Input Description")
                .productType("Input Type")
                .interest(null)
                .build();

        // Act
        Product mappedProduct = ProductMapper.toProduct(inputProduct);

        // Assert
        assertNotNull(mappedProduct);
        assertNull(mappedProduct.getInterest());
    }

    @Test
    void updateProductFromInput() {
        // Arrange
        Product existingProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Old Product")
                .description("Old Description")
                .interest(0.1)
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .productType("OLD")
                .build();

        InputProduct updatedProduct = InputProduct.builder()
                .name("Updated Product")
                .description("Updated Description")
                .interest(0.2)
                .productType("UPDATED")
                .build();

        // Act
        ProductMapper.updateProductFromInput(existingProduct, updatedProduct);

        // Assert
        assertEquals("UPDATED PRODUCT", existingProduct.getName());
        assertEquals(updatedProduct.getDescription(), existingProduct.getDescription());
        assertEquals(updatedProduct.getInterest(), existingProduct.getInterest());
        assertEquals("UPDATED", existingProduct.getProductType());
        assertNotNull(existingProduct.getUpdatedAt());
    }
}