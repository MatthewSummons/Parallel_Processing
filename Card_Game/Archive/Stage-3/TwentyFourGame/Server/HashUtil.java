package TwentyFourGame.Server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HashUtil {
    public static String SHA_256Hash(char[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(toByteArray(input));
            // Clear Array holding password data (Just to be extra safe)
            Arrays.fill(input, '0');
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // This should never happen, as SHA-256 is a standard algorithm
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private static byte[] toByteArray(char[] charArray) {
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        return byteArray;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}