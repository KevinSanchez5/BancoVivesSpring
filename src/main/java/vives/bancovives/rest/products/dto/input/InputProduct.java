package vives.bancovives.rest.products.dto.input;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputProduct {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;
    @NotBlank(message = "El tipo de producto no puede estar vacío")
    private String productType;
    @NotBlank(message = "La descripción no puede estar vacía")
    private String description;
    @Builder.Default
    @Min(value = 0, message = "El interés no puede ser negativo")
    private Double interest = 0.0;
    @Builder.Default
    private String createdAt = LocalDateTime.now().toString();
    @Builder.Default
    private String updatedAt = LocalDateTime.now().toString();
    @Builder.Default
    private Boolean isDeleted = false;
}
