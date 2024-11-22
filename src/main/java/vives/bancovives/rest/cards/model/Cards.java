package vives.bancovives.rest.cards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.products.cardtype.model.CardType;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cards {

    private UUID id;
    private String cardNumber;
    private String cardHolder;
    private LocalDate expirationDate;
    private int cvv;
    private String pin;

    private CardType cardType;

    private double dailyLimit;
    private double weeklyLimit;
    private double monthlyLimit;

    private boolean isDeleted;
    private LocalDate creationDate;
    private LocalDate lastUpdate;
}
