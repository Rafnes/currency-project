package me.dineka.currency_project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.dineka.currency_project.model.Currency;
import me.dineka.currency_project.model.ExchangeRate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CurrencyResponseDTO {
    private String name;
    private String code;
    private Integer nominal;
    private Double rate;
    private String updatedAt;
}
