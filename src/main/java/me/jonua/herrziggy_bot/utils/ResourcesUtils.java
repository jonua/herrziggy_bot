package me.jonua.herrziggy_bot.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public final class ResourcesUtils {
    public static String loadAsString(String classpath) throws IOException {
        InputStream is = ResourcesUtils.class.getClassLoader().getResourceAsStream(classpath);
        if (is == null) {
            log.error("Resource is null: {}", classpath);
            throw new RuntimeException("Resources is null");
        }
        return IOUtils.toString(is, StandardCharsets.UTF_8);
    }
}
