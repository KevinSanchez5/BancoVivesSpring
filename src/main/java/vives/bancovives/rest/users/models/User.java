package vives.bancovives.rest.users.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.utils.IdGenerator;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails, Serializable {
        @Id
        @Column(name = "id", updatable = false, nullable = false)
        @Builder.Default
        private UUID id = UUID.randomUUID();

        @Column(name = "public_id", unique = true, nullable = false)
        @Builder.Default
        private String publicId = IdGenerator.generateId();

        @Column(unique = true, nullable = false)
        @NotBlank(message = "Username no puede estar vacío")
        private String username;

        @NotBlank(message = "Password no puede estar vacío")
        @Length(min = 5, message = "Password debe tener al menos 5 caracteres")
        @Column(nullable = false)
        private String password;

        @ElementCollection(fetch = FetchType.EAGER)
        @Enumerated(EnumType.STRING)
        private Set<Role> roles;

        @OneToOne(mappedBy = "user")
        @JsonIgnoreProperties("user")
        @ToString.Exclude
        private Client client;

        @Column(updatable = false, nullable = false)
        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

        @Column(nullable = false)
        @Builder.Default
        private LocalDateTime updatedAt = LocalDateTime.now();

        @Column(nullable = false)
        @Builder.Default
        private Boolean isDeleted = false;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
                return roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .collect(Collectors.toSet());
        }

        @Override
        public boolean isEnabled() {
                return !isDeleted;
        }
}