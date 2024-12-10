package vives.bancovives.rest.movements.mapper;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.accounts.dto.output.AccountResponseSimplified;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.cards.dto.output.SimplifiedResponseCard;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.output.MovementResponseDto;
import vives.bancovives.rest.movements.model.Movement;
import vives.bancovives.rest.movements.model.MovementType;

import java.time.LocalDateTime;

@Component
public class MovementMapper {

    public Movement fromCreateDtoToEntity(MovementCreateDto createDto, Account accountOfReference, Account accountOfDestination, Card card) {
        return Movement.builder()
                .movementType(MovementType.valueOf(createDto.getMovementType().trim().toUpperCase()))
                .accountOfReference(accountOfReference)
                .accountOfDestination(accountOfDestination)
                .amountOfMoney(createDto.getAmount())
                .card(card)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public MovementResponseDto fromEntityToResponse(Movement movement) {
        return MovementResponseDto.builder()
                .id(movement.get_id())
                .movementType(movement.getMovementType().name())
                .accountOfReference(new AccountResponseSimplified(
                        movement.getAccountOfReference().getPublicId(),
                        movement.getAccountOfReference().getIban(),
                        movement.getAccountOfReference().getBalance()))
                .dniOfReference(movement.getClientOfReferenceDni())
                .ibanOfDestination(movement.getAccountOfDestination() != null ? movement.getAccountOfDestination().getIban() : null)
                .amountBeforeMovement(movement.getAmountBeforeMovement())
                .amountMoved(movement.getAmountOfMoney())
                .card(movement.getCard() != null ? new SimplifiedResponseCard(
                        movement.getCard().getPublicId(),
                        movement.getCard().getCardNumber()) : null)
                .createdAt(movement.getCreatedAt().toString())
                .build();
    }
}