package vives.bancovives.rest.movements.dtos.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import vives.bancovives.rest.accounts.dto.output.AccountResponseForClient;
import vives.bancovives.rest.cards.dto.output.SimplifiedResponseCard;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovementResponseDto {
    private ObjectId id;
    private String movementType;
    private AccountResponseForClient accountOfReference;
    private String ibanOfDestination;
    private Double amount;
    private SimplifiedResponseCard card;
    private String createdAt;

}
