package vives.bancovives.rest.products.cardtype.dto.input;

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
public class UpdatedCardType {
    @Length(min = 2, max = 30, message = "La descripción de be de estar entre 2 y 200 caracteres")
    private String name;
    @Length(min = 2, max = 200, message = "La descripción de be de estar entre 2 y 200 caracteres")
    private String description;
}
