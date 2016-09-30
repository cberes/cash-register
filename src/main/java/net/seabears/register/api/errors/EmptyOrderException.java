package net.seabears.register.api.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when an invalid operation was performed on an empty order. */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "invalid operation for an empty order")
public class EmptyOrderException extends RuntimeException {
    public EmptyOrderException(String s) {
        super(s);
    }
}
