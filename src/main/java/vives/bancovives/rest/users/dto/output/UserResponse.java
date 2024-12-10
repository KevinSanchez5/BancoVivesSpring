package vives.bancovives.rest.users.dto.output;

import vives.bancovives.rest.users.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    @Builder.Default
    private Set<Role> roles = Set.of(Role.USER);
    private Boolean isDeleted;
}