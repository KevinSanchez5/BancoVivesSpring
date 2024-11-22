package vives.bancovives.rest.products.cardtype.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "card_type")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CardType{
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    @Builder.Default
    String publicId = IdGenerator.generateId();
    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    @NotBlank(message = "La descripción no puede estar vacía")
    private String description;
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
