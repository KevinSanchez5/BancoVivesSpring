package vives.bancovives.rest.products.accounttype.service;

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
import vives.bancovives.rest.products.accounttype.dto.input.NewAccountType;
import vives.bancovives.rest.products.accounttype.dto.input.UpdatedAccountType;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.accounttype.repositories.AccountTypeRepository;
import vives.bancovives.rest.products.exceptions.ProductAlreadyExistsException;
import vives.bancovives.rest.products.exceptions.ProductDoesNotExistException;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AccountTypeServiceImplTest {

    @Mock
    private AccountTypeRepository repository;

    @InjectMocks
    private AccountTypeServiceImpl service;

    private AccountType accountType;
    private NewAccountType newAccountType;
    private UpdatedAccountType updatedAccountType;

    @BeforeEach
    void setUp() {
        accountType = AccountType.builder()
                .id(UUID.randomUUID())
                .publicId(IdGenerator.generateId())
                .name("SOMETHING")
                .description("idk")
                .interest(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        newAccountType = NewAccountType.builder()
                .name("SOMETHING")
                .interest(0.0)
                .description("idk")
                .build();

        updatedAccountType = UpdatedAccountType.builder()
                .name("UPDATED")
                .description("Updated Description")
                .interest(0.2)
                .build();
    }

    @Test
    void findById_ProductExists() {
        // Arrange
        when(repository.findByPublicId(accountType.getPublicId())).thenReturn(Optional.of(accountType));

        // Act
        AccountType result = service.findById(accountType.getPublicId());

        // Assert
        assertEquals(accountType, result);
        verify(repository, times(1)).findByPublicId(accountType.getPublicId());
    }

    @Test
    void findById_ProductDoesNotExist() {
        // Arrange
        when(repository.findByPublicId(accountType.getPublicId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> service.findById(accountType.getPublicId())
        );
    }

    @Test
    void findByName_Exists() {
        // Arrange
        when(repository.findByName(newAccountType.getName())).thenReturn(Optional.of(accountType));

        // Act
        AccountType result = service.findByName(newAccountType.getName());

        // Assert
        assertEquals(result, accountType);
        verify(repository, times(1)).findByName(newAccountType.getName());
    }

    @Test
    void findByName_DoesNotExist() {
        // Arrange
        when(repository.findByName(newAccountType.getName())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> service.findByName(newAccountType.getName())
        );
    }

    @Test
    void save_ProductWithTheSameNameDoesNotAlreadyExist() {
        // Arrange
        when(repository.findByName(newAccountType.getName())).thenReturn(Optional.empty());
        when(repository.save(any(AccountType.class))).thenReturn(accountType);

        // Act
        AccountType result = service.save(newAccountType);

        // Assert
        assertNotNull(result);
        assertEquals(accountType.getName(), result.getName());
        verify(repository, times(1)).save(any(AccountType.class));
    }

    @Test
    void save_ProductWithTheSameNameAlreadyExists() {
        // Arrange
        when(repository.findByName(newAccountType.getName())).thenReturn(Optional.of(accountType));

        // Act & Assert
        assertThrows(
                ProductAlreadyExistsException.class,
                () -> service.save(newAccountType)
        );
    }

    @Test
    void deleteById_ProductExists() {
        // Arrange
        when(repository.findByPublicId(accountType.getPublicId())).thenReturn(Optional.of(accountType));
        when(repository.save(any(AccountType.class))).thenReturn(accountType);

        // Act
        AccountType result = service.delete(accountType.getPublicId());

        // Assert
        assertTrue(result.getIsDeleted());
        verify(repository, times(1)).save(accountType);
    }

    @Test
    void deleteById_ProductDoesNotExist() {
        // Arrange
        when(repository.findByPublicId(accountType.getPublicId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> service.delete(accountType.getPublicId())
        );
    }

    @Test
    void updateById_ProductExistsAndValidUpdate() {
        // Arrange
        AccountType updatedAccountMock = AccountType.builder()
                .name("UPDATED")
                .description("idk2")
                .interest(0.2)
                .build();
        when(repository.findByPublicId(accountType.getPublicId())).thenReturn(Optional.of(accountType));
        when(repository.save(any(AccountType.class))).thenReturn(updatedAccountMock);

        // Act
        AccountType result = service.update(accountType.getPublicId(), updatedAccountType);

        // Assert
        assertNotNull(result);
        assertEquals("UPDATED", result.getName());
        verify(repository, times(1)).save(any(AccountType.class));
    }

    @Test
    void updateById_ProductDoesNotExist() {
        // Arrange
        when(repository.findByPublicId(accountType.getPublicId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductDoesNotExistException.class,
                () -> service.update(accountType.getPublicId(), updatedAccountType)
        );
    }

    @Test
    void updateById_ProductWithThatNameAlreadyExists() {
        // Arrange
        AccountType anotherProductWithDifferentIdAndTheSameName = AccountType.builder()
                .id(UUID.randomUUID())
                .name("UPDATED").build();

        when(repository.findByPublicId(accountType.getPublicId())).thenReturn(Optional.of(accountType));
        when(repository.findByName(anyString())).thenReturn(Optional.of(anotherProductWithDifferentIdAndTheSameName));

        // Act & Assert
        assertThrows(
                ProductAlreadyExistsException.class,
                () -> service.update(accountType.getPublicId(), updatedAccountType)
        );

    }

    @Test
    void findAll_ProductsFound() {
        // Arrange
        Page<AccountType> productPage = new PageImpl<>(List.of(accountType));
        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(productPage);

        // Act
        Page<AccountType> result = service.findAll(Optional.empty(), Optional.empty(), Optional.empty(), PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }
}
