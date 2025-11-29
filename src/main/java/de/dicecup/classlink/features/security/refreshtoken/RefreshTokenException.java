package de.dicecup.classlink.features.security.refreshtoken;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class RefreshTokenException extends RuntimeException {

    public RefreshTokenException(String message) {
        super(message);
    }
}
