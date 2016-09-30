package net.seabears.register.api.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when the quantity of an item on an order is invalid (i.e. zero or negative). */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "item quantity must be greater than zero")
public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException(String s) {
        super(s);
    }
}
