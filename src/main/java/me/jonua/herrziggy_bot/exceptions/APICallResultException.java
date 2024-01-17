package me.jonua.herrziggy_bot.exceptions;

import lombok.Getter;

@Getter
public class APICallResultException extends RuntimeException {
    private final int code;
    private String responseMessage;

    public APICallResultException(int code, String message) {
        super(message);
        this.code = code;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
