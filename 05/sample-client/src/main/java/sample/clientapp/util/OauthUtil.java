package sample.clientapp.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class OauthUtil {
    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] octets = new byte[32];
        secureRandom.nextBytes(octets);
        return Base64url.encode(octets);
    }

    public static String generateCodeChallenge(String codeVerifier) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            md.update(codeVerifier.getBytes("ISO_8859_1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] digestBytes = md.digest();
        return Base64url.encode(digestBytes);
    }
}
