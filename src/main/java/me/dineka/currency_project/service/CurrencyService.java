package me.dineka.currency_project.service;

import lombok.extern.slf4j.Slf4j;
import me.dineka.currency_project.dto.CurrencyRequestDTO;
import me.dineka.currency_project.dto.CurrencyResponseDTO;
import me.dineka.currency_project.exception.*;
import me.dineka.currency_project.kafka.CurrencyNotificationProducer;
import me.dineka.currency_project.mapper.CurrencyMapper;
import me.dineka.currency_project.model.Currency;
import me.dineka.currency_project.model.ExchangeRate;
import me.dineka.currency_project.repository.CurrencyRepository;
import me.dineka.currency_project.validation.Validation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class CurrencyService {
    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;
    private final ActualCurrencyRateUpdateService actualCurrencyRateUpdateService;
    private final CurrencyNotificationProducer notificationProducer;

    public CurrencyService(CurrencyRepository currencyRepository, CurrencyMapper currencyMapper, ActualCurrencyRateUpdateService actualCurrencyRateUpdateService, CurrencyNotificationProducer notificationProducer) {
        this.currencyRepository = currencyRepository;
        this.currencyMapper = currencyMapper;
        this.actualCurrencyRateUpdateService = actualCurrencyRateUpdateService;
        this.notificationProducer = notificationProducer;
    }

    public Currency addCurrency(CurrencyRequestDTO dto) {
        try {
            Validation.validateCurrencyRequest(dto);
        } catch (IllegalCurrencyNameException | IllegalCurrencyCodeException | IllegalCurrencyNominalException |
                 IllegalCurrencyRateException e) {
            log.error("Ошибка валидации полей валюты при добавлении: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Ошибка добавления валюты: ", e);
            throw e;
        }
        if (currencyRepository.existsByNameIgnoreCase(dto.getName())) {
            log.error("Не удалось добавить валюту: валюта с таким названием уже существует ");
            throw new CurrencyAlreadyExistsException("Не удалось добавить валюту: валюта с таким названием  уже существует");
        }
        if (currencyRepository.existsByCodeIgnoreCase(dto.getCode())) {
            log.error("Не удалось добавить валюту: валюта с таким кодом уже существует ");
            throw new CurrencyAlreadyExistsException("Не удалось добавить валюту: валюта с таким кодом  уже существует");
        }
        Currency currency = currencyMapper.toEntity(dto);
        currencyRepository.save(currency);

        notificationProducer.sendCurrencyCreated(currency.getCode(), currency.getName());
        log.info("Добавлена валюта: {}", currency);
        return currency;
    }

    public List<CurrencyResponseDTO> getAllCurrencies() {
        List<Currency> currencyList = currencyRepository.findAll();
        List<CurrencyResponseDTO> result = currencyList.stream()
                .map(currency -> currencyMapper.toDTO(currency))
                .toList();
        return result;
    }

    public CurrencyResponseDTO getCurrencyByCode(String code) {
        Currency currency = currencyRepository.findByCodeIgnoreCase(code).orElseThrow(() -> new CurrencyNotFoundException("Валюта с кодом " + code + " не найдена"));
        return currencyMapper.toDTO(currency);
    }

    public CurrencyResponseDTO getCurrencyById(UUID id) {
        Currency currency = currencyRepository.findById(id).orElseThrow(() -> new CurrencyNotFoundException("Валюта с id " + id + " не найдена"));
        return currencyMapper.toDTO(currency);
    }

    public CurrencyResponseDTO updateCurrencyById(UUID id, CurrencyRequestDTO dto) {
        Currency currency = currencyRepository.findById(id).orElseThrow(() -> {
            log.error("Не удалось обновить валюту: валюта с id {} не найдена", id);
            return new CurrencyNotFoundException("Валюта с id " + id + " не найдена");
        });

        if (currencyRepository.existsByNameIgnoreCaseAndCodeIgnoreCase(dto.getName(), dto.getCode())
                && dto.getRate().equals(currency.getExchangeRate().getRate())
                && dto.getNominal().equals(currency.getNominal())) {
            log.error("Не удалось обновить валюту: валюта с таким названием, кодом и курсом уже существует ");
            throw new CurrencyAlreadyExistsException("Не удалось обновить валюту: валюта с таким названием, кодом и курсом уже существует");
        }

        try {
            Validation.validateCurrencyRequest(dto);
        } catch (IllegalCurrencyNameException | IllegalCurrencyCodeException | IllegalCurrencyNominalException |
                 IllegalCurrencyRateException e) {
            log.error("Ошибка валидации полей валюты при редактировании: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Ошибка редактирования валюты: ", e);
            throw e;
        }
        currency.setName(dto.getName());
        currency.setCode(dto.getCode().toUpperCase());
        currency.setNominal(dto.getNominal());

        ExchangeRate rate = currency.getExchangeRate();
        if (dto.getRate() != null && !rate.getRate().equals(dto.getRate())) {
            rate.setRate(dto.getRate());
            rate.setUpdatedAt(LocalDateTime.now());
        }
        currencyRepository.save(currency);
        log.info("Обновлена валюта: {}", currency);
        return currencyMapper.toDTO(currency);
    }

    public void deleteCurrencyById(UUID id) {
        if (!currencyRepository.existsById(id)) {
            log.error("Не удалось удалить валюту с id {}: валюта не найдена", id);
            throw new CurrencyNotFoundException("Не удалось удалить валюту: валюта с id " + id + " не найдена");
        }
        currencyRepository.deleteById(id);
        log.info("Удалена валюта с id {}", id);
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void realDailyUpdate() {
        try {
            actualCurrencyRateUpdateService.updateRates();
            log.info("Выполнено ежедневное обновление курсов валют");
        } catch (IOException e) {
            log.error("Не удалось выполнить ежедневное обновление курсов валют: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void fakeUpdate() {
        log.info("Выполняется фейковое обновление курсов валют");
        Random random = new Random();

        List<Currency> currencies = currencyRepository.findAll();
        for (Currency currency : currencies) {
            ExchangeRate rate = currency.getExchangeRate();
            double randomRate = random.nextDouble(0.1, 300.0);
            rate.setRate(randomRate);
            rate.setUpdatedAt(LocalDateTime.now());
            currencyRepository.save(currency);
            log.info("Фейковый курс: {}: {}", currency.getCode(), currency.getExchangeRate().getRate());
        }
    }
}
