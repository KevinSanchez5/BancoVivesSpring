package vives.bancovives.rest.clients.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.users.models.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Client {
    private UUID id;
    private String dni;
    private String completeName;
    private Adress adress;
    private String email;
    private int phoneNumber;
    private String photo;
    private String dniPicture;
    private List<Account> accounts;
    private User user;

    private boolean isDeleted;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;
}
