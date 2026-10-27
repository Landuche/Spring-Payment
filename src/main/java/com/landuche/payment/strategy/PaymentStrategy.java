package com.landuche.payment.strategy;

import com.landuche.payment.domain.Customer;
import com.landuche.payment.enums.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentStrategy {
    PaymentMethod getSupportedMethod();

    // Debita o valor da conta de origem, de acordo com o meio de pagamento
    void debit(Customer source, BigDecimal amount);
}
