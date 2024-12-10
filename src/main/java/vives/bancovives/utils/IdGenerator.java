package vives.bancovives.utils;

import java.security.SecureRandom;
import java.time.Instant;

public class IdGenerator {

    private IdGenerator() {}

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    private static final int RANDOM_PART_LENGTH = 8; // Random part of the ID

    public static String generateId() {
        SecureRandom random = new SecureRandom();
        StringBuilder id = new StringBuilder();

        // Add timestamp part
        long timestampNano = Instant.now().getEpochSecond() * 1_000_000_000 + System.nanoTime() % 1_000_000_000;
        id.append(encodeTimestamp(timestampNano));

        // Add random part
        for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
            int index = random.nextInt(CHARSET.length());
            id.append(CHARSET.charAt(index));
        }

        return id.toString();
    }

    /**
     * @param timestamp el T
     * @return
     */
    private static String encodeTimestamp(long timestamp) {
        StringBuilder encoded = new StringBuilder();
        while (timestamp > 0) {
            int index = (int) (timestamp % CHARSET.length());
            encoded.insert(0, CHARSET.charAt(index));
            timestamp /= CHARSET.length();
        }
        return encoded.toString();
    }
}
