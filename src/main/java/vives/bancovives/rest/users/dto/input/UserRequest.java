package vives.bancovives.rest.users.dto.input;

import jakarta.validation.constraints.NotNull;
import vives.bancovives.rest.users.models.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @NotBlank(message = "Username no puede estar vacío")
    @NotNull(message = "Username no puede ser nulo")
    private String username;

    @NotBlank(message = "Password no puede estar vacío")
    @Length(min = 5, message = "Password debe tener al menos 5 caracteres")
    private String password;

    @Builder.Default
    private Set<Role> roles = Set.of(Role.USER);

    @Builder.Default
    private Boolean isDeleted = false;
}