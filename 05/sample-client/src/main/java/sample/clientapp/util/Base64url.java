package sample.clientapp.util;

import java.util.Base64;

public class Base64url {
    public static String encode(byte[] octets) {
        String encoded = Base64.getEncoder().encodeToString(octets);
        encoded = encoded.split("=")[0];
        encoded = encoded.replace('+', '-');
        encoded = encoded.replace('/', '_');
        return encoded;
    }

    public static byte[] decode(String encodedContent) {

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
}
