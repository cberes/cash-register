package net.seabears.register.api.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "payment must be greater than or equal to the order total")
public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String s) {
        super(s);
    }
}
