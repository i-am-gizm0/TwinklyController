package net.gizm0.exception;

public class MalformedResponseException extends Exception {

    private static final long serialVersionUID = -1935807727943174642L;

    public MalformedResponseException() {}

    public MalformedResponseException(String message) {
        super(message);
    }

    public MalformedResponseException(Throwable cause) {
        super(cause);
    }

    public MalformedResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedResponseException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
