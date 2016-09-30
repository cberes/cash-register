package net.seabears.register.api.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when the amount of a payment is invalid (i.e. negative). */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "payment must be greater than zero")
public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String s) {
        super(s);
    }
}
