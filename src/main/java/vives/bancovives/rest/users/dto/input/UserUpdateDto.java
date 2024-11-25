package vives.bancovives.rest.users.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import vives.bancovives.rest.users.models.Role;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateDto {
    private String username;

    @Length(min = 5, message = "Password debe tener al menos 5 caracteres")
    private String password;

    @Builder.Default
    private Set<Role> roles = Set.of(Role.USER);

}
