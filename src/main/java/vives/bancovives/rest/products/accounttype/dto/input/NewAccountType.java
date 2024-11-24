package vives.bancovives.rest.products.accounttype.dto.input;

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
public class NewAccountType {
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;
    @NotBlank(message = "La descripción no puede estar vacía")
    private String description;
    @NotNull(message = "El interés no puede estar vacío")
    @Min(value = 0, message = "El interés no puede ser negativo")
    private Double interest;
}