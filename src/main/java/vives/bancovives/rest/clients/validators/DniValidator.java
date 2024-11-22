package vives.bancovives.rest.clients.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DniValidator implements ConstraintValidator<ValidDni, String> {

    @Override
    public boolean isValid(String dni, ConstraintValidatorContext context) {
        if (dni == null || !dni.matches("\\d{8}[A-Za-z]")) {
            return false;
        }
        String dniLetters = "TRWAGMYFPDXBNJZSQVHLCKE";
        int dniNumber = Integer.parseInt(dni.substring(0, 8));
        int expectedLetter = dniLetters.charAt(dniNumber % 23);
        return expectedLetter == Character.toUpperCase(dni.charAt(8));
    }
}
