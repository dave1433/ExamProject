package dk.easv.blsgn.intgrpbelsign.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
    private static final SecureRandom random = new SecureRandom();

    // Method to hash password
    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt);

            byte[] hashedPassword = md.digest(password.getBytes());

            // Combine salt and password hash
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Method to check if the password matches
    public static boolean checkPassword(String rawPassword, String storedHash) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);

            // Extract salt
            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, salt.length);

            // Hash the input password with the same salt
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt);
            byte[] hashedInput = md.digest(rawPassword.getBytes());

            // Compare the hashes
            byte[] storedHashBytes = new byte[combined.length - 16];
            System.arraycopy(combined, 16, storedHashBytes, 0, storedHashBytes.length);

            return MessageDigest.isEqual(hashedInput, storedHashBytes);
        } catch (Exception e) {
            return false;
        }
    }
}