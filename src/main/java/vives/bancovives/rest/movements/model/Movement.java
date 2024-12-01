package vives.bancovives.rest.movements.model;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.cards.model.Card;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("movements")
@TypeAlias("Movement")
@EntityListeners(AuditingEntityListener.class)
public class Movement {
    @Id
    @Builder.Default
    private ObjectId id = new ObjectId();

    @Enumerated(EnumType.STRING)
    @NotNull
    private MovementType movementType;

    @NotNull
    private Account accountOfReference;

    private Account accountOfDestination;

    @Builder.Default
    @NotNull
    private Double amountOfMoney = 0.01;

    private Card card;

    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

}
