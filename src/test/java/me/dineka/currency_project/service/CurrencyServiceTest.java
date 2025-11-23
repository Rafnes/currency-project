package me.dineka.currency_project.service;

import me.dineka.currency_project.dto.CurrencyRequestDTO;
import me.dineka.currency_project.dto.CurrencyResponseDTO;
import me.dineka.currency_project.exception.*;
import me.dineka.currency_project.kafka.CurrencyNotificationProducer;
import me.dineka.currency_project.mapper.CurrencyMapper;
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
import java.util.List;
import java.util.Optional;

import static me.dineka.currency_project.TestValues.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CurrencyServiceTest {
    @Mock
    private CurrencyMapper currencyMapper;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CurrencyNotificationProducer currencyNotificationProducer;

    @InjectMocks
    private CurrencyService currencyService;

    private Currency currency1;
    private Currency currency2;
    private ExchangeRate rate1;
    private ExchangeRate rate2;
    private CurrencyRequestDTO requestDto1;
    private CurrencyRequestDTO requestDto2;
    private CurrencyResponseDTO responseDto1;
    private CurrencyResponseDTO responseDto2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        rate1 = new ExchangeRate();
        rate1.setRate(VALID_RATE_1);
        rate1.setUpdatedAt(LocalDateTime.now());

        currency1 = new Currency();
        currency1.setName(VALID_NAME_1);
        currency1.setCode(VALID_CODE_1);
        currency1.setNominal(VALID_NOMINAL_1);
        currency1.setExchangeRate(rate1);

        rate2 = new ExchangeRate();
        rate2.setRate(VALID_RATE_2);
        rate2.setUpdatedAt(LocalDateTime.now());

        currency2 = new Currency();
        currency2.setName(VALID_NAME_2);
        currency2.setCode(VALID_CODE_2);
        currency2.setNominal(VALID_NOMINAL_2);
        currency2.setExchangeRate(rate2);

        requestDto1 = new CurrencyRequestDTO(
                currency1.getName(),
                currency1.getCode(),
                currency1.getNominal(),
                currency1.getExchangeRate().getRate()
        );
        requestDto2 = new CurrencyRequestDTO(
                currency2.getName(),
                currency2.getCode(),
                currency2.getNominal(),
                currency2.getExchangeRate().getRate()
        );

        responseDto1 = new CurrencyResponseDTO(
                currency1.getName(),
                currency1.getCode(),
                currency1.getNominal(),
                currency1.getExchangeRate().getRate(),
                currency1.getExchangeRate().getUpdatedAt().toString());
        responseDto2 = new CurrencyResponseDTO(
                currency2.getName(),
                currency2.getCode(),
                currency2.getNominal(),
                currency2.getExchangeRate().getRate(),
                currency2.getExchangeRate().getUpdatedAt().toString());
    }

    @Test
    @DisplayName("Положительный тест на добавление валюты")
    void addCurrency_Positive() {
        when(currencyRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(currencyRepository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(currencyMapper.toEntity(requestDto1)).thenReturn(currency1);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency1);

        //test
        Currency actual = currencyService.addCurrency(requestDto1);

        //check
        verify(currencyRepository).save(currency1);
        verify(currencyRepository).existsByNameIgnoreCase(VALID_NAME_1);
        verify(currencyRepository).existsByCodeIgnoreCase(VALID_CODE_1);
        assertNotNull(actual);
        assertEquals(VALID_NAME_1, actual.getName());
        assertEquals(VALID_CODE_1, actual.getCode());
        assertEquals(VALID_NOMINAL_1, actual.getNominal());
        assertEquals(VALID_RATE_1, actual.getExchangeRate().getRate());
    }

    @Test
    @DisplayName("Выбрасывает исключение, если название добавляемой валюты содержит цифры")
    void addCurrency_Negative_1() {
        assertThrows(IllegalCurrencyNameException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO("Валюта 181", VALID_CODE_1, VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если название добавляемой валюты не состоит из букв")
    void addCurrency_Negative_2() {
        assertThrows(IllegalCurrencyNameException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO("@@$()", VALID_CODE_1, VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если название добавляемой валюты пустое")
    void addCurrency_Negative_3() {
        assertThrows(IllegalCurrencyNameException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(" ", VALID_CODE_1, VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если название добавляемой валюты null")
    void addCurrency_Negative_4() {
        assertThrows(IllegalCurrencyNameException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(null, VALID_CODE_1, VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код добавляемой валюты содержит цифры")
    void addCurrency_Negative_5() {
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, "A42", VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код добавляемой валюты содержит больше 3 символов")
    void addCurrency_Negative_6() {
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, "PRETTY_CODE", VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код добавляемой валюты содержит меньше 3 символов")
    void addCurrency_Negative_7() {
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, "A", VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код добавляемой валюты содержит буквы и другие символы")
    void addCurrency_Negative_8() {
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, "B&&", VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код добавляемой валюты содержит не латинские буквы")
    void addCurrency_Negative_9() {
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, "ГЗЧ", VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код добавляемой валюты null")
    void addCurrency_Negative_10() {
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, null, VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если номинал добавляемой валюты равен 0")
    void addCurrency_Negative_11() {
        assertThrows(IllegalCurrencyNominalException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, VALID_CODE_1, 0, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если номинал добавляемой валюты отрицательный")
    void addCurrency_Negative_12() {
        assertThrows(IllegalCurrencyNominalException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, VALID_CODE_1, -19, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если курс добавляемой валюты равен 0.0")
    void addCurrency_Negative_13() {
        assertThrows(IllegalCurrencyRateException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, VALID_CODE_1, VALID_NOMINAL_1, 0.0)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если курс добавляемой валюты отрицательный")
    void addCurrency_Negative_14() {
        assertThrows(IllegalCurrencyRateException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, VALID_CODE_1, VALID_NOMINAL_1, -19.92)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, валюта с таким названием уже существует")
    void addCurrency_Negative_15() {
        when(currencyRepository.existsByNameIgnoreCase(VALID_NAME_1)).thenReturn(true);
        assertThrows(CurrencyAlreadyExistsException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, VALID_CODE_1, VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Выбрасывает исключение, валюта с таким кодом уже существует")
    void addCurrency_Negative_16() {
        when(currencyRepository.existsByCodeIgnoreCase(VALID_CODE_1)).thenReturn(true);
        assertThrows(CurrencyAlreadyExistsException.class, () -> currencyService.addCurrency(new CurrencyRequestDTO(VALID_NAME_1, VALID_CODE_1, VALID_NOMINAL_1, VALID_RATE_1)));
    }

    @Test
    @DisplayName("Корректно возвращает список добавленных валют")
    void getAllCurrencies() {
        when(currencyRepository.findAll()).thenReturn(List.of(currency1, currency2));
        when(currencyMapper.toDTO(currency1)).thenReturn(responseDto1);
        when(currencyMapper.toDTO(currency2)).thenReturn(responseDto2);

        //test
        List<CurrencyResponseDTO> actual = currencyService.getAllCurrencies();

        //check
        verify(currencyRepository).findAll();
        assertNotNull(actual);
        assertEquals(2, actual.size());

        assertEquals(currency1.getName(), actual.get(0).getName());
        assertEquals(currency1.getCode(), actual.get(0).getCode());
        assertEquals(currency1.getNominal(), actual.get(0).getNominal());
        assertEquals(currency1.getExchangeRate().getRate(), actual.get(0).getRate());

        assertEquals(currency2.getName(), actual.get(1).getName());
        assertEquals(currency2.getCode(), actual.get(1).getCode());
        assertEquals(currency2.getNominal(), actual.get(1).getNominal());
        assertEquals(currency2.getExchangeRate().getRate(), actual.get(1).getRate());
    }

    @Test
    @DisplayName("Корректно возвращает существующую валюту по коду")
    void getCurrencyByCode_Positive() {
        when(currencyRepository.findByCodeIgnoreCase(VALID_CODE_1)).thenReturn(Optional.of(currency1));
        when(currencyMapper.toDTO(currency1)).thenReturn(responseDto1);

        //test
        CurrencyResponseDTO actual = currencyService.getCurrencyByCode(VALID_CODE_1);

        //check
        verify(currencyRepository).findByCodeIgnoreCase(VALID_CODE_1);
        assertNotNull(actual);

        assertEquals(currency1.getName(), actual.getName());
        assertEquals(currency1.getCode(), actual.getCode());
        assertEquals(currency1.getNominal(), actual.getNominal());
        assertEquals(currency1.getExchangeRate().getRate(), actual.getRate());
    }

    @Test
    @DisplayName("Выбрасывает исключение, если валюты с запрашиваемым кодом не существует")
    void getCurrencyByCode_Negative() {
        assertThrows(CurrencyNotFoundException.class, () -> currencyService.getCurrencyByCode(VALID_CODE_1));
    }

    @Test
    @DisplayName("Корректно возвращает существующую валюту по id")
    void getCurrencyById_Positive() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        when(currencyMapper.toDTO(currency1)).thenReturn(responseDto1);

        //test
        CurrencyResponseDTO actual = currencyService.getCurrencyById(VALID_ID_1);

        //check
        verify(currencyRepository).findById(VALID_ID_1);
        assertNotNull(actual);

        assertEquals(currency1.getName(), actual.getName());
        assertEquals(currency1.getCode(), actual.getCode());
        assertEquals(currency1.getNominal(), actual.getNominal());
        assertEquals(currency1.getExchangeRate().getRate(), actual.getRate());
    }

    @Test
    @DisplayName("Выбрасывает исключение, если валюты с запрашиваемым id не существует")
    void getCurrencyById_Negative() {
        assertThrows(CurrencyNotFoundException.class, () -> currencyService.getCurrencyById(VALID_ID_1));
    }

    @Test
    @DisplayName("Положительный тест на обновление существующей валюты, разные названия и коды")
    void updateCurrency_Positive() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        when(currencyRepository.existsByNameIgnoreCaseAndCodeIgnoreCase(VALID_NAME_1, VALID_CODE_1)).thenReturn(false);
        when(currencyRepository.save(currency1)).thenReturn(currency1);
        when(currencyMapper.toDTO(currency1)).thenReturn(responseDto2);

        //test
        CurrencyResponseDTO actual = currencyService.updateCurrencyById(VALID_ID_1, requestDto2);

        //check
        verify(currencyRepository).findById(VALID_ID_1);
        verify(currencyRepository).existsByNameIgnoreCaseAndCodeIgnoreCase(VALID_NAME_2, VALID_CODE_2);
        verify(currencyRepository).save(currency1);
        verify(currencyMapper).toDTO(currency1);

        assertNotNull(actual);
        assertEquals(responseDto2.getName(), actual.getName());
        assertEquals(responseDto2.getCode(), actual.getCode());
        assertEquals(responseDto2.getNominal(), actual.getNominal());
        assertEquals(responseDto2.getRate(), actual.getRate());
        assertEquals(responseDto2.getUpdatedAt(), actual.getUpdatedAt());
    }

    @Test
    @DisplayName("Выбрасывает исключение, если уже существует валюта с такими же названием, кодом и курсом")
    void updateCurrency_Negative_1() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        when(currencyRepository.existsByNameIgnoreCaseAndCodeIgnoreCase(VALID_NAME_1, VALID_CODE_1)).thenReturn(true);

        assertThrows(CurrencyAlreadyExistsException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, requestDto1));
    }

    @Test
    @DisplayName("Положительный тест на обновление курса у существующей валюты, название и код валюты не меняются")
    void updateCurrency_Positive_2() {
        CurrencyRequestDTO request = new CurrencyRequestDTO(
                VALID_NAME_1,
                VALID_CODE_1,
                10,
                123.44
        );

        CurrencyResponseDTO response = new CurrencyResponseDTO(
                request.getName(),
                request.getCode(),
                request.getNominal(),
                request.getRate(),
                LocalDateTime.now().toString()
        );

        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        when(currencyRepository.existsByNameIgnoreCaseAndCodeIgnoreCase(VALID_NAME_1, VALID_CODE_1)).thenReturn(true);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency1);
        when(currencyMapper.toDTO(any(Currency.class))).thenReturn(response);


        //test
        CurrencyResponseDTO actual = currencyService.updateCurrencyById(VALID_ID_1, request);

        //check
        verify(currencyRepository).findById(VALID_ID_1);
        verify(currencyRepository).existsByNameIgnoreCaseAndCodeIgnoreCase(VALID_NAME_1, VALID_CODE_1);
        verify(currencyRepository).save(any(Currency.class));
        verify(currencyMapper).toDTO(any(Currency.class));

        assertNotNull(actual);
        assertEquals(response.getName(), actual.getName());
        assertEquals(response.getCode(), actual.getCode());
        assertEquals(10, actual.getNominal());
        assertEquals(123.44, actual.getRate());
        assertEquals(response.getUpdatedAt(), actual.getUpdatedAt());
    }

    @Test
    @DisplayName("Выбрасывает исключение, если название обновляемой валюты содержит цифры")
    void updateCurrency_Negative_2() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyNameException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                "111",
                VALID_CODE_1,
                VALID_NOMINAL_1,
                VALID_RATE_1
        )));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если название обновляемой валюты не состоит из букв")
    void updateCurrency_Negative_3() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyNameException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                "&&^@",
                VALID_CODE_1,
                VALID_NOMINAL_1,
                VALID_RATE_1
        )));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если название обновляемой валюты пустое")
    void updateCurrency_Negative_4() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyNameException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                " ", VALID_CODE_1,
                VALID_NOMINAL_1,
                VALID_RATE_1
        )));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если название обновляемой валюты null")
    void updateCurrency_Negative_5() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyNameException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                null,
                VALID_CODE_1,
                VALID_NOMINAL_1,
                VALID_RATE_1
        )));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код обновляемой валюты содержит цифры")
    void updateCurrency_Negative_6() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                VALID_NAME_1,
                "YT2",
                VALID_NOMINAL_1,
                VALID_RATE_1
        )));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код обновляемой валюты содержит больше 3 символов")
    void updateCurrency_Negative_7() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                VALID_NAME_1,
                "TTRRGH",
                VALID_NOMINAL_1,
                VALID_RATE_1
        )));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код обновляемой валюты содержит меньше 3 символов")
    void updateCurrency_Negative_8() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                VALID_NAME_1,
                "RT",
                VALID_NOMINAL_1,
                VALID_RATE_1
        )));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код обновляемой валюты содержит буквы и другие символы")
    void updateCurrency_Negative_9() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                VALID_NAME_1,
                "G@5",
                VALID_NOMINAL_1,
                VALID_RATE_1
        )));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если код обновляемой валюты содержит не латинские буквы")
    void updateCurrency_Negative_10() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                VALID_NAME_1,
                "НРЧ",
                VALID_NOMINAL_1,
                VALID_RATE_1
        )));

    }

    @Test
    @DisplayName("Выбрасывает исключение, если код обновляемой валюты null")
    void updateCurrency_Negative_11() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyCodeException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                VALID_NAME_1,
                null,
                VALID_NOMINAL_1,
                VALID_RATE_1
        )));

    }

    @Test
    @DisplayName("Выбрасывает исключение, если номинал обновляемой валюты равен 0")
    void updateCurrency_Negative_12() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyNominalException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                VALID_NAME_1,
                VALID_CODE_1,
                0,
                VALID_RATE_1
        )));

    }

    @Test
    @DisplayName("Выбрасывает исключение, если номинал обновляемой валюты отрицательный")
    void updateCurrency_Negative_13() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyNominalException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                VALID_NAME_1,
                VALID_CODE_1,
                -100,
                VALID_RATE_1
        )));
    }

    @Test
    @DisplayName("Выбрасывает исключение, если курс обновляемой валюты равен 0.0")
    void updateCurrency_Negative_14() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyRateException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                VALID_NAME_1,
                VALID_CODE_1,
                VALID_NOMINAL_1,
                0.0
        )));

    }

    @Test
    @DisplayName("Выбрасывает исключение, если курс обновляемой валюты отрицательный")
    void updateCurrency_Negative_15() {
        when(currencyRepository.findById(VALID_ID_1)).thenReturn(Optional.of(currency1));
        assertThrows(IllegalCurrencyRateException.class, () -> currencyService.updateCurrencyById(VALID_ID_1, new CurrencyRequestDTO(
                VALID_NAME_1,
                VALID_CODE_1,
                VALID_NOMINAL_1,
                -90.21
        )));
    }

    @Test
    @DisplayName("Положительный тест на удаление существующей валюты")
    void deleteCurrency_Positive() {
        when(currencyRepository.existsById(VALID_ID_1)).thenReturn(true);
        //test
        currencyService.deleteCurrencyById(VALID_ID_1);

        //check
        verify(currencyRepository).existsById(VALID_ID_1);
        verify(currencyRepository).deleteById(VALID_ID_1);
    }

    @Test
    @DisplayName("Выбрасывает исключение при удалении несуществующей валюты")
    void deleteCurrency_Negative() {
        assertThrows(CurrencyNotFoundException.class, () -> currencyService.deleteCurrencyById(VALID_ID_1));
    }

    @Test
    @DisplayName("Положительный тест на фейковое обновление курсов")
    void fakeUpdate_Positive() {
        List<Currency> currencies = List.of(currency1, currency2);

        when(currencyRepository.findAll()).thenReturn(currencies);
        when(currencyRepository.save(any(Currency.class))).thenAnswer(invocation -> invocation.getArgument(0));

        currencyService.fakeUpdate();

        verify(currencyRepository, times(currencies.size())).save(any(Currency.class));

        for (Currency currency : currencies) {
            assertNotNull(currency.getExchangeRate().getRate());
            assertTrue(currency.getExchangeRate().getRate() >= 0.1 && currency.getExchangeRate().getRate() <= 300.0);
        }
    }

}