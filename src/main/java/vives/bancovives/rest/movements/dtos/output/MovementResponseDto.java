package vives.bancovives.rest.movements.dtos.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.accounts.dto.output.AccountResponseSimplified;
import vives.bancovives.rest.cards.dto.output.SimplifiedResponseCard;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovementResponseDto {
    private String id;
    private String movementType;
    private AccountResponseSimplified accountOfReference;
    private String dniOfReference;
    private String ibanOfDestination;
    private Double amountBeforeMovement;
    private Double amountMoved;
    private SimplifiedResponseCard card;
    private String createdAt;

}
