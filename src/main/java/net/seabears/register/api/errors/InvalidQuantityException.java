package net.seabears.register.api.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "item quantity must be greater than 0")
public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException(String s) {
        super(s);
    }
}
