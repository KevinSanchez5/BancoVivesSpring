package vives.bancovives.rest.cards.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vives.bancovives.rest.products.cardtype.model.CardType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "cards")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @NotBlank(message = "El titular de la tarjeta no puede estar vacío")
    @Column(nullable = false)
    private String cardOwner;

    @Column(nullable = false)
    private String expirationDate;

    @Column(nullable = false)
    private Integer cvv;

    @Column(nullable = false)
    private String pin;


    @NotNull(message = "El tipo de tarjeta no puede estar vacío")
    @ManyToOne
    @JoinColumn(name = "card_type_id", referencedColumnName = "id", nullable = false)
    private CardType cardType;


    @DecimalMin(value = "0.1", message = "El límite diario no puede ser negativo")
    @Column(nullable = false)
    private double dailyLimit = 1000;

    @DecimalMin(value = "0.0", message = "El límite semanal no puede ser negativo")
    @Column(nullable = false)
    private double weeklyLimit = 5000;

    @DecimalMin(value = "0.0", message = "El límite mensual no puede ser negativo")
    @Column(nullable = false)
    private double monthlyLimit = 10000;

    @NotNull
    @Builder.Default
    private Boolean isInactive = false;

    @NotNull
    @Builder.Default
    private Boolean isDeleted = false;

    @CreatedBy
    @Builder.Default
    @NotNull
    private LocalDateTime creationDate = LocalDateTime.now();

    @LastModifiedBy
    @Builder.Default
    @NotNull
    private LocalDateTime lastUpdate = LocalDateTime.now();
}
