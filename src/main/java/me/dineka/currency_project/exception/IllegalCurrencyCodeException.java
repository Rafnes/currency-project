package me.dineka.currency_project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalCurrencyCodeException extends IllegalArgumentException {
    public IllegalCurrencyCodeException(String s) {
        super(s);
    }
}
