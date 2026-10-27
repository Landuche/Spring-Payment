package com.landuche.payment.strategy;

import com.landuche.payment.domain.Customer;
import com.landuche.payment.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public void debit(Customer source, BigDecimal amount) {
        // Cartão abate do limite de crédito, não do saldo
        if (source.getCreditLimit().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Limite de crédito insuficiente.");
        }
        source.setCreditLimit(source.getCreditLimit().subtract(amount));
    }
}
