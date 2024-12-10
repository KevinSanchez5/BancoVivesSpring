package vives.bancovives.rest.cards.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.accounts.dto.output.OutputAccount;
import vives.bancovives.rest.products.cardtype.dto.output.OutputCardType;


import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class OutputCard {
    private String id;
    private String cardNumber;
    private String cardOwner;
    private String expirationDate;
    private Integer cvv;
    private String pin;
    private OutputCardType cardType;
    private OutputAccount account;
    private double dailyLimit;
    private double weeklyLimit;
    private double monthlyLimit;
    private Boolean isInactive;
    private Boolean isDeleted;
    private String creationDate;
    private String lastUpdate;
}
