package de.dicecup.classlink.features.security.refreshtoken;

public class RefreshTokenException extends RuntimeException {

    public enum Reason {
        NOT_FOUND,
        INVALID,
        EXPIRED,
        REUSED,
        ROTATED,
        MALFORMED
    }

    private final Reason reason;

    public RefreshTokenException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public RefreshTokenException(String message) {
        this(Reason.INVALID, message);
    }

    public Reason getReason() {
        return reason;
    }
}
