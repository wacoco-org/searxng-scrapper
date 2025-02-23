package org.example.searxngscrapper.error;

public class ScraperException extends RuntimeException {
    private final ErrorType errorType;

    public ScraperException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
