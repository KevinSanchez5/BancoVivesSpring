package vives.bancovives.rest.cards.dto.input;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequestCard {

    @Pattern(regexp = "^[0-9]{1,3}$", message = "El PIN debe estar entre 0 y 999")
    private String pin;

    @Min(value = 0, message = "El límite diario no puede ser negativo")
    private Double dailyLimit;

    @Min(value = 0, message = "El límite semanal no puede ser negativo")
    private Double weeklyLimit;

    @Min(value = 0, message = "El límite mensual no puede ser negativo")
    private Double monthlyLimit;

    private Boolean isInactive;
}