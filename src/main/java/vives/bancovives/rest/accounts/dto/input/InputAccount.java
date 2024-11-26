package vives.bancovives.rest.accounts.dto.input;

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
public class InputAccount {
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
    @NotBlank(message = "El tipo de cuenta no puede estar vacío")
    private String accountType;
}
