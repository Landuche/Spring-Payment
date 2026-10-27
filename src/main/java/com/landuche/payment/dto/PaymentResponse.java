package com.landuche.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String transactionId,
        String status,
        BigDecimal amountProcessed,
        LocalDateTime timestamp
) {}