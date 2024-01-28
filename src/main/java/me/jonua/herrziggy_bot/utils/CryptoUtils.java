package me.jonua.herrziggy_bot.utils;

import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
public final class CryptoUtils {
    public static String base64Encode(String value) {
        return new String(Base64.getEncoder().encode(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decodeBase64(String base64Data) {
        return new String(Base64.getDecoder().decode(base64Data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String md5(String cacheKeyData) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(cacheKeyData.getBytes());
            return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            String errMessage = String.format("Can't make md5 from '%s': %s", cacheKeyData, e.getMessage());
            log.error(errMessage);
            throw new RuntimeException(errMessage, e);
        }
    }
}
