package vives.bancovives.rest.products.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.exceptions.ProductAlreadyExistsException;
import vives.bancovives.rest.products.exceptions.ProductDoesNotExistException;
import vives.bancovives.rest.products.model.Product;
import vives.bancovives.rest.products.repositories.ProductRepository;
import vives.bancovives.utils.PageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private InputProduct inputProduct;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(UUID.randomUUID())
                .name("SOMETHING")
                .productType("SOMETHING")
                .description("idk")
                .interest(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        inputProduct = InputProduct.builder()
                .name("SOMETHING")
                .productType("SOMETHING")
                .description("idk")
                .interest(0.0)
                .build();
    }

    @Test
    void findById_ProductExists() {
        // Arrange
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        // Act
        Product result = productService.findById(product.getId());

        // Assert
        assertEquals(product, result);
        verify(productRepository, times(1)).findById(product.getId());
    }

    @Test
    void findById_ProductDoesNotExist() {
        // Arrange
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> productService.findById(product.getId())
        );
    }

    @Test
    void findByName_Exists() {
        // Arrange
        when(productRepository.findByName(inputProduct.getName())).thenReturn(Optional.of(product));

        // Act
        Product result = productService.findByName(inputProduct.getName());

        // Assert
        assertEquals(result, product);
        verify(productRepository, times(1)).findByName(inputProduct.getName());
    }

    @Test
    void findByName_DoesNotExist() {
        // Arrange
        when(productRepository.findByName(inputProduct.getName())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> productService.findByName(inputProduct.getName())
        );
    }

    @Test
    void save_ProductWithTheSameNameDoesNotAlreadyExist() {
        // Arrange
        when(productRepository.findByName(inputProduct.getName())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        Product result = productService.save(inputProduct);

        // Assert
        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void save_ProductWithTheSameNameAlreadyExists() {
        // Arrange
        when(productRepository.findByName(inputProduct.getName())).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(
                ProductAlreadyExistsException.class,
                () -> productService.save(inputProduct)
        );
    }

    @Test
    void deleteById_ProductExists() {
        // Arrange
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        Product result = productService.deleteById(product.getId());

        // Assert
        assertTrue(result.getIsDeleted());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void deleteById_ProductDoesNotExist() {
        // Arrange
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> productService.deleteById(product.getId())
        );
    }

    @Test
    void updateById_ProductExistsAndValidUpdate() {
        // Arrange
        InputProduct updateInputProduct = inputProduct;
        Product updatedProduct = product;
        updatedProduct.setName("Updated Product");
        updateInputProduct.setName("Updated Product");

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        Product result = productService.updateById(product.getId(), updateInputProduct);

        // Assert
        assertNotNull(result);
        assertEquals("UPDATED PRODUCT", result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateById_ProductDoesNotExist() {
        // Arrange
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> productService.updateById(product.getId(), inputProduct)
        );
    }

    @Test
    void updateById_ProductWithThatNameAlreadyExists() {
        // Arrange
        InputProduct updateInputProduct = inputProduct;
        Product updatedProduct = product;
        updatedProduct.setName("Updated Product");
        updateInputProduct.setName("Updated Product");
        Product anotherProductWithDifferentIdAndTheSameName = Product.builder()
                .id(UUID.randomUUID())
                .name("Updated Product").build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.findByName(anyString())).thenReturn(Optional.of(anotherProductWithDifferentIdAndTheSameName));

        // Act & Assert
        assertThrows(
                ProductAlreadyExistsException.class,
                () -> productService.updateById(product.getId(), updateInputProduct)
        );

    }

    @Test
    void findAll_ProductsFound() {
        // Arrange
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(productPage);

        // Act
        Page<Product> result = productService.findAll(Optional.empty(), Optional.empty(), Optional.empty(), PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }
}
