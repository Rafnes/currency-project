package me.dineka.currency_project.validation;

import me.dineka.currency_project.dto.CurrencyRequestDTO;
import me.dineka.currency_project.exception.IllegalCurrencyCodeException;
import me.dineka.currency_project.exception.IllegalCurrencyNameException;
import me.dineka.currency_project.exception.IllegalCurrencyNominalException;
import me.dineka.currency_project.exception.IllegalCurrencyRateException;
import org.apache.commons.lang3.StringUtils;

public class Validation {
    public static void validateCurrencyRequest(CurrencyRequestDTO dto) {
        validateCurrencyName(dto.getName());
        validateCurrencyCode(dto.getCode());
        validateCurrencyNominal(dto.getNominal());
        validateCurrencyRate(dto.getRate());
    }

    private static void validateCurrencyName(String name) {
        if (!StringUtils.isAlphaSpace(name)) {
            throw new IllegalCurrencyNameException("Название валюты должно содержать только буквы и пробелы");
        }
    }

    private static void validateCurrencyCode(String code) {
        if (code == null || !code.matches("[A-Za-z]{3}")) {
            throw new IllegalCurrencyCodeException("Код валюты должен содержать строго 3 латинские буквы");
        }
    }

    private static void validateCurrencyNominal(int nominal) {
        if (nominal < 1) {
            throw new IllegalCurrencyNominalException("Номинал валюты не может быть меньше 1");
        }
    }

    private static void validateCurrencyRate(Double rate) {
        if (rate != null && rate <= 0.0) {
            throw new IllegalCurrencyRateException("Курс валюты должен быть больше 0.0");
        }
    }
}
