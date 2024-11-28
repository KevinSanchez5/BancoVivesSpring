package vives.bancovives.rest.movements.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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

    @NotNull
    private Account accountOfOrigin;

    private Account accountOfDestination;

    private Double amountOfMoney;

    @NotNull
    private MovementType movementType;

    private Card card;

    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @JsonProperty("id")
    public String get_id() {
        return id.toHexString();
    }



}
