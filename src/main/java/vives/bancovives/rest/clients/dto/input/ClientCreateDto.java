package vives.bancovives.rest.clients.dto.input;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import vives.bancovives.rest.clients.validators.ValidDni;

@Data
@AllArgsConstructor
public class ClientCreateDto {

    @NotNull(message = "El dni no puede ser nulo")
    @NotBlank(message = "El dni no puede estar vacio")
    @NotEmpty(message = "El dni no puede estar vacio")
    @Pattern(regexp = "^[0-9]{8}[A-Za-z]$", message = "El dni debe de tener 8 digitos y una letra")
    @ValidDni
    private String dni;

    @NotNull(message = "El nombre no puede ser nulo")
    @Length(min = 5, max = 255 , message = "El nombre tiene que tener entre 5 y 255 caracteres")
    private String completeName;

    @Email(message = "El email debe de ser valido")
    @NotNull(message = "El email no puede ser nulo")
    @NotBlank(message = "El email no puede estar vacio")
    private String email;

    @NotNull(message = "El numero no puede ser nulo")
    @Pattern(regexp = "^[679]\\d{8}$", message = "El numero de telefono debe de tener 9 digitos y comenzar por 6, 7 o 9")
    private String phoneNumber;

    private String photo;

    private String dniPicture;

    @NotNull(message = "El nombre de la calle no puede ser nulo")
    @NotBlank(message = "El nombre de la calle no puede estar vacio")
    @Length(min = 5, max = 255 , message = "El nombre de la calle tiene que tener entre 5 y 255 caracteres")
    private String street;

    @NotNull(message = "El numero de la casa no puede ser nulo")
    @NotBlank(message = "El numero de la casa no puede estar vacio")
    @Pattern(regexp = "\\d+[A-Za-z]*", message = "El numero de la casa debe ser un numero y puede tener una letra al final de manera opcional")
    private String houseNumber;

    @NotNull(message = "La ciudad no puede ser nula")
    @NotBlank(message = "La ciudad no puede estar vacia")
    @Length(min = 5, max = 90 , message = "La ciudad tiene que tener entre 5 y 90 caracteres")
    private String city;

    @NotNull(message = "El pais no puede ser nulo")
    @NotBlank(message = "El pais no puede estar vacio")
    @Length(min = 5, max = 90 , message = "El pais tiene que tener entre 5 y 90 caracteres")
    private String country;
}
