package vives.bancovives.rest.clients.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.utils.IdGenerator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "clients")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Client implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "public_id")
    private String publicId;

    @Column(name = "dni", unique = true)
    private String dni;

    @Column(name = "complete_name")
    private String completeName;

    @Embedded
    private Address address;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "photo")
    private String photo;

    @Column(name = "dni_picture")
    private String dniPicture;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("client")
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "client")
    @JsonIgnoreProperties("client")
    private List<Account> accounts;

    @Column(name= "validated")
    private boolean validated;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Client(String dni, String completeName, String email, String phoneNumber, String photo, String dniPicture) {
        this.id = UUID.randomUUID();
        this.publicId = IdGenerator.generateId();
        this.dni = dni;
        this.address = new Address();
        this.completeName = completeName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photo = null;
        this.dniPicture = null;
        this.user = null;
        this.accounts = null;
        this.validated = false;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
