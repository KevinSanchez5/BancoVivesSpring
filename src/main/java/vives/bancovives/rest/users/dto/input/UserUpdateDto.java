package vives.bancovives.rest.users.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.users.models.Role;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateDto {
    private String username;

    private String password;

    @Builder.Default
    private Set<Role> roles = Set.of(Role.USER);

}
