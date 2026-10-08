package com.lankabuild.cms.exception;

/** Thrown by the service layer; carries an HTTP status code to send back to the client. */
public class ApiException extends RuntimeException {
    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() { return statusCode; }
}
