package me.jonua.herrziggy_bot.utils;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.exceptions.APICallException;
import me.jonua.herrziggy_bot.exceptions.APICallExecutionException;
import me.jonua.herrziggy_bot.exceptions.APICallResultException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Slf4j
public final class RetrofitUtils {
    public static <T> void execute(Call<T> call) throws APICallException {
        tryExecute(call);
    }

    public static <T> T executeWithResult(Call<T> call) {
        return tryExecute(call).body();
    }

    private static <T> Response<T> tryExecute(Call<T> call) throws APICallException {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                log.trace("API call executed");
                return response;
            } else {
                String errorBody = extractErrorBody(response).orElse("");
                log.error("API respond with error: {} {}", response.code(), errorBody);
                APICallResultException exception = new APICallResultException(response.code(), "API respond with error: " + errorBody);
                Optional<String> responseMessage = JsonUtils.extractAsString(errorBody, "message");
                if (responseMessage.isPresent()) {
                    log.error("API call response message is {}", responseMessage.get());
                    exception.setResponseMessage(responseMessage.get());
                }

                throw exception;
            }
        } catch (IOException e) {
            log.error("Unable to execute API call: {}", e.getMessage());
            throw new APICallExecutionException("Unable to execute api call", e);
        }
    }

    private static <T> Optional<String> extractErrorBody(Response<T> response) {
        try (ResponseBody responseBody = response.errorBody()) {
            if (responseBody != null) {
                try {
                    return of(responseBody.string());
                } catch (IOException e) {
                    log.error("Unable to extract error message from error body of the bad API response: {}", e.getMessage());
                }
            }
        }
        return empty();
    }
}
