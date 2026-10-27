package com.landuche.payment.observer;

import com.landuche.payment.dto.PaymentResponse;

public record PaymentProcessedEvent(PaymentResponse response, String payerId) {}