package vives.bancovives.rest.clients.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "clients")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "id_path")
    private String idPath;

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

//TODO : añadir el storage de las imagenes
    @Column(name = "photo")
    private String photo;

    @Column(name = "dni_picture")
    private String dniPicture;


// TODO : Añadir relaciones con otras entidades
//    @OneToMany(mappedBy = "client", orphanRemoval = false)
//    private List<Account> accounts;
//    @OneToOne(mappedBy = "client", orphanRemoval = false)
//    private User user;


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
        this.idPath = null;
        this.dni = dni;
        this.address = new Address();
        this.completeName = completeName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photo = null;
        this.dniPicture = null;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
