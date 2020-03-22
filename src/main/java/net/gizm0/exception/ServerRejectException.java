package net.gizm0.exception;

public class ServerRejectException extends Exception {

    private static final long serialVersionUID = 2811624491570019044L;

    public ServerRejectException() {}

    public ServerRejectException(String message) {
        super(message);
    }

    public ServerRejectException(Throwable cause) {
        super(cause);
    }

    public ServerRejectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerRejectException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
