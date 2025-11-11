package me.dineka.currency_project.service;

import lombok.extern.slf4j.Slf4j;
import me.dineka.currency_project.dto.CurrencyRequestDTO;
import me.dineka.currency_project.dto.CurrencyResponseDTO;
import me.dineka.currency_project.exception.*;
import me.dineka.currency_project.mapper.CurrencyMapper;
import me.dineka.currency_project.model.Currency;
import me.dineka.currency_project.model.ExchangeRate;
import me.dineka.currency_project.repository.CurrencyRepository;
import me.dineka.currency_project.validation.Validation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
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
        if (currencyRepository.existsByNameIgnoreCaseAndCodeIgnoreCase(dto.getName(), dto.getCode())) {
            log.error("Не удалось добавить валюту: валюта с таким названием и кодом уже существует ");
            throw new CurrencyAlreadyExistsException("Не удалось добавить валюту: валюта с таким названием и кодом уже существует");
        }
        Currency currency = CurrencyMapper.toEntity(dto);
        currencyRepository.save(currency);
        log.info("Добавлена валюта: {}", currency);
        return currency;
    }

    public List<CurrencyResponseDTO> getAllCurrencies() {
        List<Currency> currencyList = currencyRepository.findAll();
        List<CurrencyResponseDTO> result = currencyList.stream()
                .map(currency -> CurrencyMapper.toDTO(currency))
                .toList();
        return result;
    }

    public CurrencyResponseDTO getCurrencyByCode(String code) {
        Currency currency = currencyRepository.findByCodeIgnoreCase(code).orElseThrow(() -> new CurrencyNotFoundException("Валюта с кодом " + code + " не найдена"));
        return CurrencyMapper.toDTO(currency);
    }

    public CurrencyResponseDTO getCurrencyById(UUID id) {
        Currency currency = currencyRepository.findById(id).orElseThrow(() -> new CurrencyNotFoundException("Валюта с id " + id + " не найдена"));
        return CurrencyMapper.toDTO(currency);
    }

    public CurrencyResponseDTO updateCurrencyById(UUID id, CurrencyRequestDTO dto) {
        Currency currency = currencyRepository.findById(id).orElseThrow(() -> {
            log.error("Не удалось обновить валюту: валюта с id {} не найдена", id);
            return new CurrencyNotFoundException("Валюта с id " + id + " не найдена");
        });

        if (currencyRepository.existsByNameIgnoreCaseAndCodeIgnoreCase(dto.getName(), dto.getCode()) && dto.getRate().equals(currency.getExchangeRate().getRate())) {
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
        return CurrencyMapper.toDTO(currency);
    }

    public void deleteCurrencyById(UUID id) {
        if (!currencyRepository.existsById(id)) {
            log.error("Не удалось удалить валюту с id {}: валюта не найдена", id);
            throw new CurrencyNotFoundException("Не удалось удалить валюту: валюта с id " + id + " не найдена");
        }
        currencyRepository.deleteById(id);
        log.info("Удалена валюта с id {}", id);
    }
}
