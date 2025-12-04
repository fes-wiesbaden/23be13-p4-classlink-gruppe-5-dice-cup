package de.dicecup.classlink.features.security;

public class AccessTokenExpiredException extends RuntimeException {
    public AccessTokenExpiredException(String message) {
        super(message);
    }
}
