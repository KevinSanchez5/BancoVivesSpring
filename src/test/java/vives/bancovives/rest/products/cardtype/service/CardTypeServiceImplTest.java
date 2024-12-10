package vives.bancovives.rest.products.cardtype.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import vives.bancovives.rest.products.cardtype.dto.input.NewCardType;
import vives.bancovives.rest.products.cardtype.dto.input.UpdatedCardType;
import vives.bancovives.rest.products.cardtype.model.CardType;
import vives.bancovives.rest.products.cardtype.repositories.CardTypeRepository;
import vives.bancovives.rest.products.exceptions.ProductAlreadyExistsException;
import vives.bancovives.rest.products.exceptions.ProductDoesNotExistException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardTypeServiceImplTest {

    @Mock
    private CardTypeRepository repository;

    @InjectMocks
    private CardTypeServiceImpl service;

    private CardType cardType;
    private NewCardType newCardType;
    private UpdatedCardType updatedCardType;

    @BeforeEach
    void setUp() {
        cardType = CardType.builder()
                .id(UUID.randomUUID())
                .name("SOMETHING")
                .description("idk")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        newCardType = NewCardType.builder()
                .name("SOMETHING")
                .description("idk")
                .build();

        updatedCardType = UpdatedCardType.builder()
                .name("UPDATED")
                .description("Updated Description")
                .build();
    }

    @Test
    void findById_ProductExists() {
        // Arrange
        when(repository.findByPublicId(cardType.getPublicId())).thenReturn(Optional.of(cardType));

        // Act
        CardType result = service.findById(cardType.getPublicId());

        // Assert
        assertEquals(cardType, result);
        verify(repository, times(1)).findByPublicId(cardType.getPublicId());
    }

    @Test
    void findById_ProductDoesNotExist() {
        // Arrange
        when(repository.findByPublicId(cardType.getPublicId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> service.findById(cardType.getPublicId())
        );
    }

    @Test
    void findByName_Exists() {
        // Arrange
        when(repository.findByName(newCardType.getName())).thenReturn(Optional.of(cardType));

        // Act
        CardType result = service.findByName(newCardType.getName());

        // Assert
        assertEquals(result, cardType);
        verify(repository, times(1)).findByName(newCardType.getName());
    }

    @Test
    void findByName_DoesNotExist() {
        // Arrange
        when(repository.findByName(newCardType.getName())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> service.findByName(newCardType.getName())
        );
    }

    @Test
    void save_ProductWithTheSameNameDoesNotAlreadyExist() {
        // Arrange
        when(repository.findByName(newCardType.getName())).thenReturn(Optional.empty());
        when(repository.save(any(CardType.class))).thenReturn(cardType);

        // Act
        CardType result = service.save(newCardType);

        // Assert
        assertNotNull(result);
        assertEquals(cardType.getName(), result.getName());
        verify(repository, times(1)).save(any(CardType.class));
    }

    @Test
    void save_ProductWithTheSameNameAlreadyExists() {
        // Arrange
        when(repository.findByName(newCardType.getName())).thenReturn(Optional.of(cardType));

        // Act & Assert
        assertThrows(
                ProductAlreadyExistsException.class,
                () -> service.save(newCardType)
        );
    }

    @Test
    void deleteById_ProductExists() {
        // Arrange
        when(repository.findByPublicId(cardType.getPublicId())).thenReturn(Optional.of(cardType));
        when(repository.save(any(CardType.class))).thenReturn(cardType);

        // Act
        CardType result = service.delete(cardType.getPublicId());

        // Assert
        assertTrue(result.getIsDeleted());
        verify(repository, times(1)).save(cardType);
    }

    @Test
    void deleteById_ProductDoesNotExist() {
        // Arrange
        when(repository.findByPublicId(cardType.getPublicId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> service.delete(cardType.getPublicId())
        );
    }

    @Test
    void updateById_ProductExistsAndValidUpdate() {
        // Arrange
        CardType updatedAccountMock = CardType.builder()
                .name("UPDATED")
                .description("idk2")
                .build();
        when(repository.findByPublicId(cardType.getPublicId())).thenReturn(Optional.of(cardType));
        when(repository.save(any(CardType.class))).thenReturn(updatedAccountMock);

        // Act
        CardType result = service.update(cardType.getPublicId(), updatedCardType);

        // Assert
        assertNotNull(result);
        assertEquals("UPDATED", result.getName());
        verify(repository, times(1)).save(any(CardType.class));
    }

    @Test
    void updateById_ProductDoesNotExist() {
        // Arrange
        when(repository.findByPublicId(cardType.getPublicId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> service.update(cardType.getPublicId(), updatedCardType)
        );
    }

    @Test
    void updateById_ProductWithThatNameAlreadyExists() {
        // Arrange
        CardType anotherProductWithDifferentIdAndTheSameName = CardType.builder()
                .id(UUID.randomUUID())
                .name("UPDATED").build();

        when(repository.findByPublicId(cardType.getPublicId())).thenReturn(Optional.of(cardType));
        when(repository.findByName(anyString())).thenReturn(Optional.of(anotherProductWithDifferentIdAndTheSameName));

        // Act & Assert
        assertThrows(
                ProductAlreadyExistsException.class,
                () -> service.update(cardType.getPublicId(), updatedCardType)
        );

    }

    @Test
    void findAll_ProductsFound() {
        // Arrange
        Page<CardType> productPage = new PageImpl<>(List.of(cardType));
        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(productPage);

        // Act
        Page<CardType> result = service.findAll(Optional.empty(), Optional.empty(), PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }
}