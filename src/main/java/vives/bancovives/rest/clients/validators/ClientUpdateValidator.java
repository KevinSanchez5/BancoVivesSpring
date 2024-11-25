package vives.bancovives.rest.clients.validators;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.exceptions.ClientBadRequest;

@Component
public class ClientUpdateValidator {

    public void validateUpdateDto(ClientUpdateDto dto) {
        if (dto.getDni() == null && dto.getCompleteName() == null && dto.getEmail() == null && dto.getPhoneNumber() == null && dto.getPhoto() == null && dto.getDniPicture() == null && dto.getStreet() == null && dto.getHouseNumber() == null && dto.getCity() == null && dto.getCountry() == null && dto.getUsername() == null && dto.getPassword() == null) {
            throw new ClientBadRequest("Debes introducir al menos un campo para actualizar");
        }
        if (dto.getDni() != null && !dto.getDni().matches("^[0-9]{8}[A-Za-z]$")) {
            throw new ClientBadRequest("El dni debe de tener 8 digitos y una letra");
        }
        if (dto.getCompleteName() != null && dto.getCompleteName().trim().isBlank() && (dto.getCompleteName().length() < 5 || dto.getCompleteName().length() > 255)) {
            throw new ClientBadRequest("El nombre tiene que tener entre 5 y 255 caracteres");
        }
        if (dto.getEmail() != null &&  dto.getEmail().trim().isBlank()  && !dto.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,6}$")) {
            throw new ClientBadRequest("El email debe de ser valido");
        }
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().matches("^[679]\\d{8}$")) {
            throw new ClientBadRequest("El numero de telefono debe de tener 9 digitos y comenzar por 6, 7 o 9");
        }
        if (dto.getStreet() != null && dto.getStreet().trim().isBlank()  && (dto.getStreet().length() < 5 || dto.getStreet().length() > 255)) {
            throw new ClientBadRequest("El nombre de la calle tiene que tener entre 5 y 255 caracteres");
        }
        if (dto.getHouseNumber() != null && dto.getHouseNumber().trim().isBlank() && !dto.getHouseNumber().matches("\\d+[A-Za-z]*")) {
            throw new ClientBadRequest("El numero de la casa debe ser un numero y puede tener una letra al final de manera opcional");
        }
        if (dto.getCity() != null && dto.getCity().trim().isBlank() && (dto.getCity().length() < 5 || dto.getCity().length() > 90)) {
            throw new ClientBadRequest("La ciudad tiene que tener entre 5 y 90");
        }
        if (dto.getCountry() != null && dto.getCountry().trim().isBlank() && (dto.getCountry().length() < 5 || dto.getCountry().length() > 90)) {
            throw new ClientBadRequest("El pais tiene que tener entre 5 y 90 caracteres");
        }
        if (dto.getDniPicture() != null && dto.getDniPicture().trim().isBlank()) {
            throw new ClientBadRequest("La imagen del dni no puede estar vacia");
        }
        if (dto.getPhoto() != null && dto.getPhoto().trim().isBlank()) {
            throw new ClientBadRequest("La imagen del cliente no puede estar vacia");
        }
        if (dto.getUsername()!= null && dto.getUsername().trim().isBlank()) {
            throw new ClientBadRequest("El nombre de usuario no puede estar vacio");
        }
        if (dto.getPassword() != null && dto.getPassword().trim().isBlank() && (dto.getPassword().length() < 5 || dto.getPassword().length() > 255)) {
            throw new ClientBadRequest("La contraseña tiene que tener entre 5 y 255 caracteres");
        }
    }
}