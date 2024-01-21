package me.jonua.herrziggy_bot.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class CryptoUtils {
    public static String base64Encode(String value) {
        return new String(Base64.getEncoder().encode(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decodeBase64(String base64Data) {
        return new String(Base64.getDecoder().decode(base64Data.getBytes(StandardCharsets.UTF_8)));
    }
}
