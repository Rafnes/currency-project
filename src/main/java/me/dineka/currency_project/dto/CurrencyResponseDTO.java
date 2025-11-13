package me.dineka.currency_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
