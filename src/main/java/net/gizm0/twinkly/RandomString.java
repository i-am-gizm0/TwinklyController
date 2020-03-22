package net.gizm0.twinkly;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * A class to generate a random string, used for authenticating to Twinkly
 */
public class RandomString {

    /**
     * Generate a random string.
     */
    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

    public static final String base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890/+";

    private final Random random;

    private final char[] symbols;

    private final char[] buf;

    /**
     * Create a string generator
     * @param length the length of the string to generate
     * @param random the generator to use
     * @param symbols the characters to use
     */
    public RandomString(int length, Random random, String symbols) {
        if (length < 1) throw new IllegalArgumentException();
        if (symbols.length() < 2) throw new IllegalArgumentException();
        this.random = Objects.requireNonNull(random);
        this.symbols = symbols.toCharArray();
        this.buf = new char[length];
    }

    /**
     * Create an base64eric string generator.
     * @param length the length of the string to generate
     * @param random the generator to use
     */
    public RandomString(int length, Random random) {
        this(length, random, base64);
    }

    /**
     * Create an base64eric strings from a secure generator.
     * @param length the length of string to generate
     */
    public RandomString(int length) {
        this(length, new SecureRandom());
    }

    /**
     * Create session identifiers.
     */
    public RandomString() {
        this(21);
    }

}