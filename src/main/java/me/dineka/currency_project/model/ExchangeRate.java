package me.dineka.currency_project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "exchange_rate")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(mappedBy = "exchangeRate")
    private Currency currency;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRate that = (ExchangeRate) o;
        return Objects.equals(id, that.id) && Objects.equals(currency, that.currency) && Objects.equals(rate, that.rate) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, currency, rate, updatedAt);
    }

    @Override
    public String toString() {
        return "ExchangeRate{" +
                "id=" + id +
                ", currency=" + currency +
                ", rate=" + rate +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
