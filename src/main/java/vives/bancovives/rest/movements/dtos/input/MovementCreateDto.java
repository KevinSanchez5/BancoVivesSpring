package vives.bancovives.rest.movements.dtos.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovementCreateDto {

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @NotBlank(message = "El tipo de movimiento es obligatorio")
    private String movementType;

    @NotNull(message = "El IBAN de referencia es obligatorio")
    @NotBlank(message = "El IBAN de referencia es obligatorio")
    private String ibanOfReference;


    private String ibanOfDestination;
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor que 0")
    private Double amount;

    private String cardNumber;

}
