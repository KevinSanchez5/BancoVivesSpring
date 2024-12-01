package vives.bancovives.rest.movements.mapper;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.accounts.dto.output.AccountResponseForClient;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.cards.dto.output.SimplifiedResponseCard;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.output.MovementResponseDto;
import vives.bancovives.rest.movements.model.Movement;
import vives.bancovives.rest.movements.model.MovementType;

import java.time.LocalDateTime;
import java.util.Optional;

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
                .id(movement.getId())
                .movementType(movement.getMovementType().name())
                .accountOfReference(new AccountResponseForClient(
                        movement.getAccountOfReference().getPublicId(),
                        movement.getAccountOfReference().getIban(),
                        movement.getAccountOfReference().getBalance()))
                .ibanOfDestination(movement.getAccountOfDestination().getIban() != null ? movement.getAccountOfDestination().getIban() : null)
                .amount(movement.getAmountOfMoney())
                .card(movement.getCard() != null ? new SimplifiedResponseCard(
                        movement.getCard().getPublicId(),
                        movement.getCard().getCardNumber()) : null)
                .createdAt(movement.getCreatedAt().toString())
                .build();
    }
}