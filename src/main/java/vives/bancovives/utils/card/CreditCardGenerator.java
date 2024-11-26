package vives.bancovives.utils.card;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.cards.model.Card;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class CreditCardGenerator {
    private static final Random random = new Random();

    public static String generateCardNumber() {
        int[] cardNumber = new int[16];
        for (int i = 0; i < 15; i++) {
            cardNumber[i] = random.nextInt(10);
        }
        cardNumber[15] = getCheckDigit(cardNumber);
        StringBuilder cardNumberStr = new StringBuilder();
        for (int digit : cardNumber) {
            cardNumberStr.append(digit);
        }
        return cardNumberStr.toString();
    }

    private static int getCheckDigit(int[] cardNumber) {
        int sum = 0;
        boolean isSecond = true;
        for (int i = cardNumber.length - 2; i >= 0; i--) {
            int digit = cardNumber[i];
            if (isSecond) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            isSecond = !isSecond;
        }
        return (10 - (sum % 10)) % 10;
    }

    public static String generateExpirationDate() {
        LocalDate expirationDate = LocalDate.now().plusYears(3).withDayOfMonth(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        return expirationDate.format(formatter);
    }

    public static Integer generateCVV() {
        return random.nextInt(900) + 100;
    }

    public static void generateCardDetails(Card card) {
        card.setCardNumber(generateCardNumber());
        card.setExpirationDate(generateExpirationDate());
        card.setCvv(generateCVV());
    }
}