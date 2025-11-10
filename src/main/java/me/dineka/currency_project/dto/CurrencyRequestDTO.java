package me.dineka.currency_project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CurrencyRequestDTO {
    private String name;
    private String code;
    private Integer nominal;
}
