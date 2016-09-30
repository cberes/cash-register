package net.seabears.register.api.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when the tax % on an order is invalid (i.e. negative). */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "order tax must be greater than or equal to 0")
public class InvalidTaxException extends RuntimeException {
    public InvalidTaxException(String s) {
        super(s);
    }
}
