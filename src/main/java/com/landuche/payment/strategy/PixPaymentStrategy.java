package com.landuche.payment.strategy;

import com.landuche.payment.domain.Customer;
import com.landuche.payment.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PixPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.PIX;
    }

    @Override
    public void debit(Customer source, BigDecimal amount) {
        // PIX debita do saldo em conta corrente
        if (source.getAccountBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente em conta corrente.");
        }
        source.setAccountBalance(source.getAccountBalance().subtract(amount));
    }
}
