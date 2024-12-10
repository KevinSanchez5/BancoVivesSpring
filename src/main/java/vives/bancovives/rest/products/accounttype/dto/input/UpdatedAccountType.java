package vives.bancovives.rest.products.accounttype.dto.input;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedAccountType {
    @Length(min = 2, max = 30, message = "La descripción de be de estar entre 2 y 200 caracteres")
    private String name;
    @Length(min = 2, max = 200, message = "La descripción de be de estar entre 2 y 200 caracteres")
    private String description;
    @Min(value = 0, message = "El interés no puede ser negativo")
    private Double interest;
}
