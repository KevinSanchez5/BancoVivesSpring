package vives.bancovives.utils.account;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Component
public class IbanGenerator {

    private static final Map<String, Integer> COUNTRY_IBAN_LENGTHS = new HashMap<>();
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    static {
        COUNTRY_IBAN_LENGTHS.put("ES", 24); // España
        COUNTRY_IBAN_LENGTHS.put("DE", 22); // Alemania
        COUNTRY_IBAN_LENGTHS.put("FR", 27); // Francia
        // Añade otros países y longitudes según sea necesario
    }

    /**
     * Genera un IBAN para un país específico.
     *
     * @param countryCode Código del país en formato ISO 3166-1 alpha-2 (e.g., "ES", "DE").
     * @return Un IBAN generado.
     * @throws IllegalArgumentException Si el país no es soportado.
     */
    public static String generateIban(String countryCode) {
        Integer ibanLength = COUNTRY_IBAN_LENGTHS.get(countryCode);
        if (ibanLength == null) {
            throw new IllegalArgumentException("Código de país no soportado: " + countryCode);
        }

        StringBuilder iban = new StringBuilder(countryCode);
        iban.append("00"); // Los dos dígitos de control (se calculan por separado)

        while (iban.length() < ibanLength) {
            iban.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }

        // Calcula los dígitos de control y actualiza el IBAN
        String checkDigits = calculateCheckDigits(iban.toString());
        iban.replace(2, 4, checkDigits);

        return iban.toString();
    }

    /**
     * Calcula los dígitos de control para un IBAN.
     *
     * @param iban El IBAN (sin los dígitos de control calculados).
     * @return Los dígitos de control calculados como un String de dos caracteres.
     */
    private static String calculateCheckDigits(String iban) {
        String reformattedIban = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numericIban = new StringBuilder();

        for (char c : reformattedIban.toCharArray()) {
            if (Character.isDigit(c)) {
                numericIban.append(c);
            } else {
                numericIban.append(c - 'A' + 10);
            }
        }

        int mod = mod97(numericIban.toString());
        int checkDigits = 98 - mod;

        return String.format("%02d", checkDigits);
    }

    /**
     * Calcula el módulo 97 según el estándar ISO 7064.
     *
     * @param numericIban El IBAN representado como un número entero largo.
     * @return El resultado del módulo 97.
     */
    private static int mod97(String numericIban) {
        int remainder = 0;

        for (int i = 0; i < numericIban.length(); i++) {
            int digit = Character.getNumericValue(numericIban.charAt(i));
            remainder = (remainder * 10 + digit) % 97;
        }

        return remainder;
    }
}
