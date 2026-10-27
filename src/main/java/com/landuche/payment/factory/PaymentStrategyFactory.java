package com.landuche.payment.factory;

import com.landuche.payment.enums.PaymentMethod;
import com.landuche.payment.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PaymentStrategy::getSupportedMethod, Function.identity()));
    }

    public PaymentStrategy getStrategy(PaymentMethod paymentMethod) {
        return Optional.ofNullable(strategies.get(paymentMethod))
                .orElseThrow(() -> new IllegalArgumentException("Meio de pagamento não suportado: " + paymentMethod));
    }
}
