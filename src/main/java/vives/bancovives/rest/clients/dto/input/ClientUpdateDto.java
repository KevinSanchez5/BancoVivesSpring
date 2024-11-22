package vives.bancovives.rest.clients.dto.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import vives.bancovives.rest.clients.validators.ValidDni;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientUpdateDto {

    @Pattern(regexp = "^[0-9]{8}[A-Za-z]$", message = "El dni debe de tener 8 digitos y una letra")
    @ValidDni
    private String dni;

    @Length(min = 5, max = 255 , message = "El nombre tiene que tener entre 5 y 255 caracteres")
    private String completeName;

    @Email(message = "El email debe de ser valido")
    private String email;

    @Pattern(regexp = "^[679]\\d{8}$", message = "El numero de telefono debe de tener 9 digitos y comenzar por 6, 7 o 9")
    private String phoneNumber;

    private String photo;
    private String dniPicture;

    @Length(min = 5, max = 255 , message = "El nombre de la calle tiene que tener entre 5 y 255 caracteres")
    private String street;
    @Pattern(regexp = "\\d+[A-Za-z]*", message = "El numero de la casa debe ser un numero y puede tener una letra al final de manera opcional")
    private String houseNumber;
    @Length(min = 5, max = 90 , message = "La ciudad tiene que tener entre 5 y 90 caracteres")
    private String city;
    @Length(min = 5, max = 90 , message = "El pais tiene que tener entre 5 y 90 caracteres")
    private String country;


}
