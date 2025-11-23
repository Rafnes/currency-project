package me.dineka.currency_project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ExchangeRateUpdateException extends RuntimeException {
    public ExchangeRateUpdateException(String message) {
        super(message);
    }
}
