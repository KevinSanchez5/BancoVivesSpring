package vives.bancovives.rest.accounts.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.products.model.ProductAccount;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    private UUID id;
    private String iban;
    private double balance;
    private String password;

    private Client client;
    private ProductAccount accountType;

    private boolean isDeleted;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;
}
