package com.oursaviorgames.backend.model.response;

/**
 * Represents a boolean response with an optional reason.
 */
public class BooleanResponse {

    private final Boolean result;
    private final String reason;

    public BooleanResponse(Boolean result) {
        this.result = result;
        this.reason = null;
    }

    public BooleanResponse(Boolean result, String reason) {
        this.result = result;
        this.reason = reason;
    }

    public Boolean getResult() {
        return result;
    }

    public String getReason() {
        return reason;
    }

}