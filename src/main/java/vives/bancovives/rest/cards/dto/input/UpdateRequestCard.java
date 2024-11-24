package vives.bancovives.rest.cards.dto.input;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequestCard {
    @NotBlank(message = "El titular de la tarjeta no puede estar vacío")
    private String cardOwner;

    @NotNull(message = "El PIN no puede estar vacío")
    private Integer pin;

 /*   @NotNull(message = "El tipo de tarjeta no puede estar vacío")
    private Product cardType;*/

    @Min(value = 0, message = "El límite diario no puede ser negativo")
    private Double dailyLimit;

    @Min(value = 0, message = "El límite semanal no puede ser negativo")
    private Double weeklyLimit;

    @Min(value = 0, message = "El límite mensual no puede ser negativo")
    private Double monthlyLimit;
}