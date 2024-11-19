package vives.bancovives.rest.users.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

        private UUID id;
        private String username;
        private String password;
        private String email;
        private Role role;

        private boolean isDeleted;
        private String creationDate;
        private String lastUpdate;
}