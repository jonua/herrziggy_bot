package me.jonua.herrziggy_bot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

@Slf4j
public class JsonUtils {
    public static Optional<String> extractAsString(String rawObject, String key) {
        return buildJsonObject(rawObject)
                .filter(o -> o.has(key))
                .map(o -> o.get(key))
                .filter(String.class::isInstance)
                .map(String.class::cast);
    }

    @NotNull
    private static Optional<JSONObject> buildJsonObject(String rawObject) {
        try {
            return Optional.of(new JSONObject(rawObject));
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    @NotNull
    public static <T> String objectToJsonString(ObjectMapper mapper, T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize an object {}: {}", object, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
