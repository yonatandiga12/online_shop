package BusinessLayer.Users;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Password {

    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(password.getBytes());

        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hashedBytes) {
            stringBuilder.append(String.format("%02x", b));
        }

        return stringBuilder.toString();
    }

    public static boolean verifyPassword(String password, String hashedPassword) throws NoSuchAlgorithmException {
        String hashedInput = hashPassword(password);
        return hashedInput.equals(hashedPassword);
    }
}