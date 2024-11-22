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
    @DecimalMin(value = "0.0", message = "El saldo no puede ser negativo")
    private double balance;
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}
