package vives.bancovives.rest.clients.validators;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.exceptions.ClientBadRequest;

@Component
public class ClientUpdateValidator {

    public void validateUpdateDto(ClientUpdateDto dto) {
        
        validateNotNullAtributes(dto.getDni(), dto.getCompleteName(),dto.getEmail(), dto.getPhoneNumber(),dto.getPhoto(), dto.getDniPicture(), dto.getStreet(), dto.getHouseNumber() ,dto.getCity(),dto.getCountry(),dto.getUsername(),dto.getPassword());
        validateDni(dto.getDni());
        validateCompleteName(dto.getCompleteName());
        validateEmail(dto.getCompleteName());
        validatePhoneNumber(dto.getPhoneNumber());
        validateStreet(dto.getStreet());
        validateHouseNumber(dto.getHouseNumber());
        validateCity(dto.getCity());
        validateCountry(dto.getCountry());
        validatedniPicture(dto.getDniPicture());
        validatePhoto(dto.getPhoto());
        validateUsername(dto.getUsername());
        validatePassword(dto.getPassword());
    }
    
    private void validateNotNullAtributes (String dni, String completeName, String email, String phoneNumber, String photo, String dniPicture, String street, String houseNumber, String city, String country, String username, String password){
        if (dni == null && completeName == null && email == null && phoneNumber == null && photo == null && dniPicture == null && street == null && houseNumber == null && city == null && country == null && username == null && password == null) {
            throw new ClientBadRequest("Debes introducir al menos un campo para actualizar");
        }
    }
    
    private void validateDni(String dni){
        if (dni != null && !dni.matches("^[0-9]{8}[A-Za-z]$")) {
            throw new ClientBadRequest("El dni debe de tener 8 digitos y una letra");
        }
    }
    
    private void validateCompleteName(String completeName){
        if (completeName != null && completeName.trim().isBlank() && (completeName.length() < 5 || completeName.length() > 255)) {
            throw new ClientBadRequest("El nombre tiene que tener entre 5 y 255 caracteres");
        }
    }
    
    private void validateEmail(String email){
        if (email != null &&  email.trim().isBlank()  && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,6}$")) {
            throw new ClientBadRequest("El email debe de ser valido");
        }
    }

    private void validatePhoneNumber(String phoneNumber){
        if (phoneNumber != null && !phoneNumber.matches("^[679]\\d{8}$")) {
            throw new ClientBadRequest("El numero de telefono debe de tener 9 digitos y comenzar por 6, 7 o 9");
        }
    }

    private void validateStreet(String street){
        if (street != null && street.trim().isBlank()  && (street.length() < 5 || street.length() > 255)) {
            throw new ClientBadRequest("El nombre de la calle tiene que tener entre 5 y 255 caracteres");
        }
    }
    private void validateHouseNumber(String houseNumber){
        if (houseNumber != null && houseNumber.trim().isBlank() && !houseNumber.matches("\\d+[A-Za-z]*")) {
            throw new ClientBadRequest("El numero de la casa debe ser un numero y puede tener una letra al final de manera opcional");
        }
    }
    private void validateCity(String city){
        if (city != null && city.trim().isBlank() && (city.length() < 5 || city.length() > 90)) {
            throw new ClientBadRequest("La ciudad tiene que tener entre 5 y 90");
        }
    }
    private void validateCountry(String country){
        if (country != null && country.trim().isBlank() && (country.length() < 5 || country.length() > 90)) {
            throw new ClientBadRequest("El pais tiene que tener entre 5 y 90 caracteres");
        }
    }
    private void validatedniPicture(String dniPicture){
        if (dniPicture != null && dniPicture.trim().isBlank()) {
            throw new ClientBadRequest("La imagen del dni no puede estar vacia");
        }
    }
    private void validatePhoto(String photo){
        if (photo != null && photo.trim().isBlank()) {
            throw new ClientBadRequest("La imagen del cliente no puede estar vacia");
        }
    }
    private void validateUsername(String username){
        if (username!= null && username.trim().isBlank()) {
            throw new ClientBadRequest("El nombre de usuario no puede estar vacio");
        }
    }
    private void validatePassword(String password){
        if (password != null && password.trim().isBlank() && (password.length() < 5 || password.length() > 255)) {
            throw new ClientBadRequest("La contraseña tiene que tener entre 5 y 255 caracteres");
        }
    }
}