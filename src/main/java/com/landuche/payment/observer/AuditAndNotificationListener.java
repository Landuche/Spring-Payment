package com.landuche.payment.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuditAndNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(AuditAndNotificationListener.class);

    @EventListener
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        // Auditoria do pagamento aprovado (aqui poderia entrar uma notificação ao cliente)
        log.info("Pagamento aprovado | transação={} | valor={} | pagador={}",
                event.response().transactionId(), event.response().amountProcessed(), event.payerId());
    }
}
