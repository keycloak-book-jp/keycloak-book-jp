package sample.oauthutil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class OauthUtil {
    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] octets = new byte[32];
        secureRandom.nextBytes(octets);
        return encodeToBase64Url(octets);
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
        return encodeToBase64Url(digestBytes);
    }

    public static <T> T readJsonContent(byte[] content, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        T readValue = null;
        try {
            readValue = mapper.readValue(content, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readValue;
    }

    public static String encodeToBase64Url(byte[] octets) {
        String encoded = Base64.getEncoder().encodeToString(octets);
        encoded = encoded.split("=")[0];
        encoded = encoded.replace('+', '-');
        encoded = encoded.replace('/', '_');
        return encoded;
    }

    public static byte[] decodeFromBase64Url(String encoded) {
        String[] parts = encoded.split("\\.");
        String encodedContent = parts[1];

        encodedContent = encodedContent.replace('-', '+');
        encodedContent = encodedContent.replace('_', '/');
        switch (encodedContent.length() % 4) {
            case 0:
                break;
            case 2:
                encodedContent += "==";
                break;
            case 3:
                encodedContent += "=";
                break;
        }

        return Base64.getDecoder().decode(encodedContent);
    }

    public static String encodeToBasicClientCredential(String clientId, String clientSecret) {
        String clientcredential = clientId + ":" + clientSecret;
        return Base64.getEncoder().encodeToString(clientcredential.getBytes());
    }
}
