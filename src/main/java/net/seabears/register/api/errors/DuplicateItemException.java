package net.seabears.register.api.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "the item was already added to the order")
public class DuplicateItemException extends RuntimeException {
    public DuplicateItemException(String s) {
        super(s);
    }
}
