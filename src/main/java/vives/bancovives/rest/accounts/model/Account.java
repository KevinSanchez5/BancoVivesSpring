package vives.bancovives.rest.accounts.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.mapping.DBRef;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.utils.IdGenerator;
import vives.bancovives.utils.account.IbanGenerator;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account implements Serializable {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column
    @Builder.Default
    private String publicId = IdGenerator.generateId();

    @Column(nullable = false, unique = true)
    private String iban;

    @Column(nullable = false)
    private double balance = 0.0;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;


    @ManyToOne
    @JoinColumn(name = "account_type", nullable = false)
    @DBRef(lazy = true)
    private AccountType accountType;


    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties("accounts")
    @DBRef(lazy = true)
    private Client client;

    @Builder.Default
    @NotNull
    @CreatedBy
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @NotNull
    @LastModifiedBy
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder.Default
    @NotNull
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    // Método para generar el IBAN automáticamente
    @PrePersist
    private void generateIban() {
        if (this.iban == null || this.iban.isEmpty()) {
            this.iban = IbanGenerator.generateIban("ES");
        }
    }
}