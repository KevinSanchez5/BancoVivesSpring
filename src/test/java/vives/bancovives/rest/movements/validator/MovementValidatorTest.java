package vives.bancovives.rest.movements.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vives.bancovives.rest.accounts.exception.AccountNotFoundException;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.repositories.AccountRepository;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.repository.CardsRepository;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.exceptions.MovementBadRequest;
import vives.bancovives.rest.movements.exceptions.MovementForbidden;
import vives.bancovives.rest.products.accounttype.model.AccountType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementValidatorTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardsRepository cardRepository;

    @InjectMocks
    private MovementValidator movementValidator;

    private MovementCreateDto movementCreateDto;


    @BeforeEach
    void setUp() {
        movementCreateDto = new MovementCreateDto();
        movementCreateDto.setAmount(100.0);
        movementCreateDto.setMovementType("TRANSFERENCIA");
        movementCreateDto.setIbanOfReference("ES123456789");
        movementCreateDto.setIbanOfDestination("ES987654321");
    }


    @Test
    void validateMovementDto_WhenTransferenciaWithValidData_ShouldNotThrowException() {
        Account accountOfReference = new Account();
        accountOfReference.setBalance(500.0);
        accountOfReference.setDeleted(false);

        Account accountOfDestination = new Account();
        accountOfDestination.setDeleted(false);

        when(accountRepository.findByIban("ES123456789")).thenReturn(Optional.of(accountOfReference));
        when(accountRepository.findByIban("ES987654321")).thenReturn(Optional.of(accountOfDestination));

        assertDoesNotThrow(() -> movementValidator.validateMovementDto(movementCreateDto));
        verify(accountRepository, times(2)).findByIban(any());

    }

    @Test
    void validateMovementDto_WhenReferenceAccountNotFound_ShouldThrowAccountNotFoundException() {
        when(accountRepository.findByIban("ES123456789")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> movementValidator.validateMovementDto(movementCreateDto));
    }

    @Test
    void validateMovementType_WhenInvalidType_ShouldThrowMovementBadRequest() {
        movementCreateDto.setMovementType("INVALIDO");

        assertThrows(MovementBadRequest.class, () -> movementValidator.validateMovementType(movementCreateDto.getMovementType()));
    }

    @Test
    void validatePositiveAmount_WhenNegativeAmount_ShouldThrowMovementBadRequest() {
        movementCreateDto.setAmount(-100.0);

        assertThrows(MovementBadRequest.class, () -> movementValidator.validatePositiveAmount(movementCreateDto.getAmount()));
    }

    @Test
    void validateTransferencia_WhenInsufficientFunds_ShouldThrowMovementBadRequest() {
        Account accountOfReference = new Account();
        accountOfReference.setBalance(50.0);
        accountOfReference.setDeleted(false);

        Account accountOfDestination = new Account();
        accountOfDestination.setDeleted(false);

        when(accountRepository.findByIban("ES123456789")).thenReturn(Optional.of(accountOfReference));
        when(accountRepository.findByIban("ES987654321")).thenReturn(Optional.of(accountOfDestination));

        assertThrows(MovementBadRequest.class, () -> movementValidator.validateMovementDto(movementCreateDto));
    }

    @Test
    void validateInteresMensual_WhenAccountTypeHasNoInterest_ShouldThrowMovementBadRequest() {
        Account account = new Account();
        account.setDeleted(false);
        AccountType accountType = AccountType.builder().interest(0.0).build();
        account.setAccountType(accountType);

        when(accountRepository.findByIban("ES123456789")).thenReturn(Optional.of(account));

        movementCreateDto.setMovementType("INTERESMENSUAL");

        assertThrows(MovementForbidden.class, () -> movementValidator.validateMovementDto(movementCreateDto));
    }



    @Test
    void validatePago_WhenCardIsDeleted_ShouldThrowMovementBadRequest() {
        MovementCreateDto dto = new MovementCreateDto("PAGO", "IBAN123", null, 50.0, "CARD123");
        Account account = Account.builder().iban("IBAN123").balance(500.0).isDeleted(false).build();
        Card card = Card.builder().cardNumber("CARD123").account(account).isInactive(false).isDeleted(true).expirationDate("12/25").build();

        when(accountRepository.findByIban("IBAN123")).thenReturn(Optional.of(account));
        when(cardRepository.findByCardNumber("CARD123")).thenReturn(Optional.of(card));

        MovementBadRequest exception = assertThrows(MovementBadRequest.class, () ->
                movementValidator.validateMovementDto(dto)
        );

        assertEquals("La tarjeta esta eliminada", exception.getMessage());
    }

    @Test
    void validateIngreso_WhenCardIsInactive_ShouldThrowMovementBadRequest() {
        MovementCreateDto dto = new MovementCreateDto("INGRESO", "IBAN123", null, 200.0,"CARD123");
        Account account = Account.builder().iban("IBAN123").balance(500.0).isDeleted(false).build();
        Card card = Card.builder().cardNumber("CARD123").account(account).isInactive(true).isDeleted(false).expirationDate("12/25").build();

        when(accountRepository.findByIban("IBAN123")).thenReturn(Optional.of(account));
        when(cardRepository.findByCardNumber("CARD123")).thenReturn(Optional.of(card));

        MovementBadRequest exception = assertThrows(MovementBadRequest.class, () ->
                movementValidator.validateMovementDto(dto)
        );

        assertEquals("La tarjeta esta inactiva", exception.getMessage());
    }


    @Test
    void validateCard_WhenCardIsExpired_ShouldThrowMovementBadRequest() {
        Card card = Card.builder().cardNumber("CARD123").isInactive(false).isDeleted(false).expirationDate("12/20").build();

        MovementBadRequest exception = assertThrows(MovementBadRequest.class, () ->
                movementValidator.validateCardIsValid(card)
        );

        assertEquals("La tarjeta esta caducada", exception.getMessage());
    }

    @Test
    void validateNomina_WhenAccountIsDeleted_ShouldThrowMovementBadRequest() {
        MovementCreateDto dto = new MovementCreateDto("NOMINA", "IBAN123", null, 1000.0, null);
        Account account = Account.builder().iban("IBAN123").balance(500.0).isDeleted(true).build();

        when(accountRepository.findByIban("IBAN123")).thenReturn(Optional.of(account));

        MovementBadRequest exception = assertThrows(MovementBadRequest.class, () ->
                movementValidator.validateMovementDto(dto)
        );

        assertEquals("La cuenta esta eliminada", exception.getMessage());
    }

    @Test
    void validateTransferencia_WhenDestinationAccountDeleted_ShouldThrowMovementBadRequest() {
        MovementCreateDto dto = new MovementCreateDto("TRANSFERENCIA", "IBAN123", "IBAN456",  500.0,null);
        Account accountOfReference = Account.builder().iban("IBAN123").balance(500.0).isDeleted(false).build();
        Account accountOfDestination = Account.builder().iban("IBAN456").balance(500.0).isDeleted(true).build();// Cuenta eliminada

        when(accountRepository.findByIban("IBAN123")).thenReturn(Optional.of(accountOfReference));
        when(accountRepository.findByIban("IBAN456")).thenReturn(Optional.of(accountOfDestination));

        MovementBadRequest exception = assertThrows(MovementBadRequest.class, () ->
                movementValidator.validateMovementDto(dto)
        );

        assertEquals("La cuenta esta eliminada", exception.getMessage());
    }


    @Test
    void validatePago_WhenAmountIsNegative_ShouldThrowMovementBadRequest() {
        MovementCreateDto dto = new MovementCreateDto("PAGO", "IBAN123", null, -100.0,"CARD123");
        Account accountOfReference = Account.builder().iban("IBAN123").balance(500.0).isDeleted(false).build();
        Card card =  Card.builder().cardNumber("CARD123").account(accountOfReference).isInactive(false).isDeleted(false).expirationDate("12/25").build();

        MovementBadRequest exception = assertThrows(MovementBadRequest.class, () ->
                movementValidator.validateMovementDto(dto)
        );

        assertEquals("La cantidad a mover debe ser mayor a 0", exception.getMessage());
    }


    @Test
    void validateIngreso_WhenAccountDoesNotExist_ShouldThrowMovementBadRequest() {
        MovementCreateDto dto = new MovementCreateDto("INGRESO", "IBAN123", null,300.0, null);

        when(accountRepository.findByIban("IBAN123")).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                movementValidator.validateMovementDto(dto)
        );

        assertEquals("La cuenta con el IBAN IBAN123 no existe", exception.getMessage());
    }

}