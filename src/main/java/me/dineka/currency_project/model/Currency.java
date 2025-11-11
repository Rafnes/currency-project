package me.dineka.currency_project.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "currency")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "nominal")
    private Integer nominal;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "exchange_rate_id")
    private ExchangeRate exchangeRate;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        Currency currency = (Currency) obj;
        return Objects.equals(id, currency.id) && Objects.equals(name, currency.name) && Objects.equals(code, currency.code) && Objects.equals(nominal, currency.nominal) && Objects.equals(exchangeRate, currency.exchangeRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, code, nominal, exchangeRate);
    }

    @Override
    public String toString() {
        return "Currency {" +
                "id=" + id +
                ", name=" + name +
                ", code=" + code +
                ", nominal=" + nominal +
                '}';
    }
}
