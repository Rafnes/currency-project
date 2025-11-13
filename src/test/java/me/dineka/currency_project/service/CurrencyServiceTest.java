package me.dineka.currency_project.service;

import me.dineka.currency_project.TestValues;
import me.dineka.currency_project.dto.CurrencyRequestDTO;
import me.dineka.currency_project.model.Currency;
import me.dineka.currency_project.model.ExchangeRate;
import me.dineka.currency_project.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static me.dineka.currency_project.TestValues.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Положительный тест на добавление валюты")
    void addCurrency_Positive() {
        UUID currencyId = VALID_ID_1;
        UUID rateId = VALID_ID_2;

        ExchangeRate rate = new ExchangeRate();
        rate.setId(rateId);
        rate.setRate(VALID_RATE_1);
        rate.setUpdatedAt(LocalDateTime.now());

        Currency currency = new Currency();
        currency.setId(currencyId);
        currency.setName(VALID_NAME_1);
        currency.setCode(VALID_CODE_1);
        currency.setNominal(VALID_NOMINAL_1);
        currency.setExchangeRate(rate);

        CurrencyRequestDTO requestDTO = new CurrencyRequestDTO(VALID_NAME_1, VALID_CODE_1, VALID_NOMINAL_1, VALID_RATE_1);


        when(currencyRepository.existsByNameIgnoreCaseAndCodeIgnoreCase(anyString(), anyString())).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        //test
        Currency actual = currencyService.addCurrency(requestDTO);

        //check
        verify(currencyRepository).save(currency);
        verify(currencyRepository).existsByNameIgnoreCaseAndCodeIgnoreCase(VALID_NAME_1, VALID_CODE_1);
        assertNotNull(actual);
        assertEquals(VALID_NAME_1, actual.getName());
        assertEquals(VALID_CODE_1, actual.getCode());
        assertEquals(VALID_NOMINAL_1, actual.getNominal());
        assertEquals(VALID_RATE_1, actual.getExchangeRate().getRate());
    }
}