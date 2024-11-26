package vives.bancovives.rest.products.accounttype.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vives.bancovives.utils.IdGenerator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "account_type")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AccountType implements Serializable {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    @NotBlank
    @Builder.Default
    String publicId = IdGenerator.generateId();
    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    @NotBlank(message = "La descripción no puede estar vacía")
    private String description;
    @Builder.Default
    @Min(value = 0, message = "El interés no puede ser negativo")
    private Double interest = null;
    @CreatedBy
    @Builder.Default
    @NotNull
    private LocalDateTime createdAt = LocalDateTime.now();
    @LastModifiedBy
    @Builder.Default
    @NotNull
    private LocalDateTime updatedAt = LocalDateTime.now();
    @NotNull
    @Builder.Default
    private Boolean isDeleted = false;
}
