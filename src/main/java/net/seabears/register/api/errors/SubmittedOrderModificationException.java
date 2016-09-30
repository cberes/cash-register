package net.seabears.register.api.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown after an attempt to modify an already submitted order. */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "cannot modify a submitted order")
public class SubmittedOrderModificationException extends RuntimeException {
    public SubmittedOrderModificationException(String s) {
        super(s);
    }
}
