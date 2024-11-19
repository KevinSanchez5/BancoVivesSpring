package vives.bancovives.rest.products.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "account_types")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Product{
    @Id
    private UUID id;
    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    @NotBlank(message = "El tipo de producto no puede estar vacío")
    private String productType;
    @Column(nullable = false)
    @NotBlank(message = "La descripción no puede estar vacía")
    private String description;
    @Builder.Default
    @Min(value = 0, message = "El interés no puede ser negativo")
    private Double interest = 0.0;
    @CreatedBy
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @LastModifiedBy
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    @NotNull
    @Builder.Default
    private Boolean isDeleted = false;
}
