package vives.bancovives.rest.clients.validators;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.exceptions.ClientBadRequest;

/**
 * Clase que valida los campos de un cliente a la hora de actualizarlo.
 */
@Component
public class ClientUpdateValidator {

    /**
     * Método que valida los campos de un cliente a la hora de actualizarlo.
     * @param dto Objeto de la clase ClientUpdateDto que se va a validar
     */
    public void validateUpdateDto(ClientUpdateDto dto) {
        validateNotNullAtributes(dto.getDni(), dto.getCompleteName(),dto.getEmail(), dto.getPhoneNumber(), dto.getStreet(), dto.getHouseNumber() ,dto.getCity(),dto.getCountry(),dto.getUsername(),dto.getPassword());
        validateDni(dto.getDni());
        validateCompleteName(dto.getCompleteName());
        validateEmail(dto.getEmail());
        validatePhoneNumber(dto.getPhoneNumber());
        validateStreet(dto.getStreet());
        validateHouseNumber(dto.getHouseNumber());
        validateCity(dto.getCity());
        validateCountry(dto.getCountry());
        validateUsername(dto.getUsername());
        validatePassword(dto.getPassword());
    }

    /**
     * Método que valida que al menos un campo no sea nulo.
     * @param dni
     * @param completeName
     * @param email
     * @param phoneNumber
     * @param street
     * @param houseNumber
     * @param city
     * @param country
     * @param username
     * @param password
     */
    private void validateNotNullAtributes (String dni, String completeName, String email, String phoneNumber, String street, String houseNumber, String city, String country, String username, String password){
        if (dni == null && completeName == null && email == null && phoneNumber == null && street == null && houseNumber == null && city == null && country == null && username == null && password == null) {
            throw new ClientBadRequest("Debes introducir al menos un campo para actualizar");
        }
    }

    /**
     * Método que valida el dni.
     * @param dni
     */
    private void validateDni(String dni){
        if (dni != null && !dni.matches("^[0-9]{8}[A-Za-z]$")) {
            throw new ClientBadRequest("El dni debe de tener 8 digitos y una letra");
        }
    }

    /**
     * Método que valida el nombre completo.
     * @param completeName
     */
    private void validateCompleteName(String completeName){
        if (completeName != null && (completeName.length() < 5 || completeName.length() > 255)) {
            throw new ClientBadRequest("El nombre tiene que tener entre 5 y 255 caracteres");
        }
    }

    /**
     * Método que valida el email.
     * @param email Email a validar.
     */
    private void validateEmail(String email){
        if (email != null && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new ClientBadRequest("El email debe de ser valido");
        }
    }

    /**
     * Método que valida el numero de telefono.
     * @param phoneNumber Numero de telefono a validar.
     */
    private void validatePhoneNumber(String phoneNumber){
        if (phoneNumber != null && !phoneNumber.matches("^[679]\\d{8}$")) {
            throw new ClientBadRequest("El numero de telefono debe de tener 9 digitos y comenzar por 6, 7 o 9");
        }
    }

    /**
     * Método que valida la calle.
     * @param street Calle a validar.
     */
    private void validateStreet(String street){
        if (street != null && (street.length() < 5 || street.length() > 255)) {
            throw new ClientBadRequest("El nombre de la calle tiene que tener entre 5 y 255 caracteres");
        }
    }

    /**
     * Método que valida el numero de la casa.
     * @param houseNumber
     */
    private void validateHouseNumber(String houseNumber){
        if (houseNumber != null && !houseNumber.matches("\\d+[A-Za-z]*")) {
            throw new ClientBadRequest("El numero de la casa debe ser un numero y puede tener una letra al final de manera opcional");
        }
    }

    /**
     * Método que valida la ciudad.
     * @param city
     */
    private void validateCity(String city){
        if (city != null && city.trim().isBlank() && (city.length() < 5 || city.length() > 90)) {
            throw new ClientBadRequest("La ciudad tiene que tener entre 5 y 90");
        }
    }

    /**
     * Método que valida el pais.
     * @param country
     */
    private void validateCountry(String country){
        if (country != null && country.trim().isBlank() && (country.length() < 5 || country.length() > 90)) {
            throw new ClientBadRequest("El pais tiene que tener entre 5 y 90 caracteres");
        }
    }

    /**
     * Método que valida el nombre de usuario
     * @param username
     */
    private void validateUsername(String username){
        if (username!= null && username.trim().isBlank()) {
            throw new ClientBadRequest("El nombre de usuario no puede estar vacio");
        }
    }
    /**
     * Valida la contrasñea
     * El metodo comprubea si la contraseña cumple los siguientes requisitos:
     * - Not null
     * - Not blank (after trimming)
     * - Length between 5 and 255 characters
     *
     * @param password
     * @throws ClientBadRequest si la contraseña es null, blank, o fuera del rango
     */
    private void validatePassword(String password){
        if (password != null && password.trim().isBlank() && (password.length() < 5 || password.length() > 255)) {
            throw new ClientBadRequest("La contraseña tiene que tener entre 5 y 255 caracteres");
        }
    }
}