package vives.bancovives.rest.cards.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class OutputCard {
    private UUID id;
    private String cardNumber;
    private String cardOwner;
    private String expirationDate;
    private Integer cvv;
    private Integer pin;
    private String cardType;
    private double dailyLimit;
    private double weeklyLimit;
    private double monthlyLimit;
    private Boolean isInactive;
    private Boolean isDeleted;
    private String creationDate;
    private String lastUpdate;
}
