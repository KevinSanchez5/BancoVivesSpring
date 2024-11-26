package vives.bancovives.rest.accounts.dto.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.clients.validators.ValidDni;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputAccount {
    @DecimalMin(value = "0.0", message = "El saldo no puede ser negativo")
    private double balance;
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
    @NotBlank(message = "El DNI no puede estar vacío")
    @ValidDni
    @NotNull(message = "El DNI no puede ser nulo")
    private String dni;
}
