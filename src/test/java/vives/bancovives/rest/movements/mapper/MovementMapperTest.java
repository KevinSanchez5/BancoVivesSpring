package vives.bancovives.rest.movements.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.model.Movement;
import vives.bancovives.rest.movements.model.MovementType;

import static org.junit.jupiter.api.Assertions.*;

class MovementMapperTest {

    Account accountOfReference ;
    Account accountOfDestination ;
    Card card ;
    MovementCreateDto movementCreateDto ;
    Movement movement ;
    private MovementMapper movementMapper ;

    @BeforeEach
    void setUp() {
        movementMapper = new MovementMapper();
        accountOfReference = Account.builder().iban("ibanOfReference").balance(100).build();
        accountOfDestination = Account.builder().iban("ibanOfDestination").balance(1).build();
        card = Card.builder().publicId("123").cardNumber("123").build();
        movementCreateDto = new MovementCreateDto("TRANSFERENCIA", accountOfDestination.getIban(), accountOfDestination.getIban(), 10.0,"123");
    }

    @Test
    void fromCreateDtoToEntity() {
        Movement movement = movementMapper.fromCreateDtoToEntity(movementCreateDto, accountOfReference, accountOfDestination, card);
        assertAll(
                () -> assertEquals(MovementType.TRANSFERENCIA, movement.getMovementType()),
                () -> assertEquals(accountOfReference, movement.getAccountOfReference()),
                () -> assertEquals(accountOfDestination, movement.getAccountOfDestination()),
                () -> assertEquals(10.0, movement.getAmountOfMoney()),
                () -> assertEquals(card, movement.getCard()),
                () -> assertFalse(movement.getIsDeleted()),
                () -> assertNotNull(movement.getCreatedAt()),
                () -> assertNotNull(movement.getUpdatedAt())
        );

    }

    @Test
    void fromEntityToResponse() {
        movement = Movement.builder()
                .movementType(MovementType.TRANSFERENCIA)
                .accountOfReference(accountOfReference)
                .accountOfDestination(accountOfDestination)
                .amountOfMoney(10.0)
                .card(card)
                .isDeleted(false)
                .createdAt(null)
                .updatedAt(null)
                .build();
        Movement movement = movementMapper.fromCreateDtoToEntity(movementCreateDto, accountOfReference, accountOfDestination, card);
        assertAll(
                () -> assertEquals(MovementType.TRANSFERENCIA, movement.getMovementType()),
                () -> assertEquals(accountOfReference, movement.getAccountOfReference()),
                () -> assertEquals(accountOfDestination, movement.getAccountOfDestination()),
                () -> assertEquals(10.0, movement.getAmountOfMoney()),
                () -> assertEquals(card, movement.getCard()),
                () -> assertFalse(movement.getIsDeleted()),
                () -> assertNotNull(movement.getCreatedAt()),
                () -> assertNotNull(movement.getUpdatedAt())
        );
    }
}