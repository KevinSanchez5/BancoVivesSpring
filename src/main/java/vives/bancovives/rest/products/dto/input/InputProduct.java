package vives.bancovives.rest.products.dto.input;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputProduct {
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;
    @NotBlank(message = "El tipo de producto no puede estar vacío")
    private String productType;
    @NotBlank(message = "La descripción no puede estar vacía")
    private String description;
    @Builder.Default
    @Min(value = 0, message = "El interés no puede ser negativo")
    private Double interest = null;
}
