package vives.bancovives.rest.cards.dto.input;

import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputCard {
    @NotBlank(message = "El titular de la tarjeta no puede estar vacío")
    @Column(nullable = false)
    private String cardOwner;

    @Column(nullable = false)
    private String pin;

    @NotBlank(message = "El tipo de tarjeta no puede estar vacío")
    private String cardTypeName;

    @DecimalMin(value = "0.1", message = "El límite diario no puede ser negativo")
    @Column(nullable = false)
    @Builder.Default
    private double dailyLimit = 1000;

    @DecimalMin(value = "0.0", message = "El límite semanal no puede ser negativo")
    @Column(nullable = false)
    @Builder.Default
    private double weeklyLimit = 5000;

    @DecimalMin(value = "0.0", message = "El límite mensual no puede ser negativo")
    @Column(nullable = false)
    @Builder.Default
    private double monthlyLimit = 10000;
}
