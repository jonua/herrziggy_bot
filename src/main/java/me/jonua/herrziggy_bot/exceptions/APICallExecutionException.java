package me.jonua.herrziggy_bot.exceptions;

import java.io.IOException;

public class APICallExecutionException extends RuntimeException {
    public APICallExecutionException(String message, Exception cause) {
        super(message, cause);
    }
}
