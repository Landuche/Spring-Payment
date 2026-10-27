package com.landuche.payment.service;

import com.landuche.payment.domain.Customer;
import com.landuche.payment.domain.PaymentTransaction;
import com.landuche.payment.dto.PaymentResponse;
import com.landuche.payment.enums.PaymentMethod;
import com.landuche.payment.enums.TransactionStatus;
import com.landuche.payment.factory.PaymentStrategyFactory;
import com.landuche.payment.observer.PaymentProcessedEvent;
import com.landuche.payment.repository.CustomerRepository;
import com.landuche.payment.repository.TransactionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentStrategyFactory factory;
    private final ApplicationEventPublisher eventPublisher;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public PaymentService(PaymentStrategyFactory factory,
                          ApplicationEventPublisher eventPublisher,
                          CustomerRepository customerRepository,
                          TransactionRepository transactionRepository) {
        this.factory = factory;
        this.eventPublisher = eventPublisher;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public PaymentResponse processPayment(String sourceAccountNumber, String destinationIdentifier,
                                          BigDecimal amount, PaymentMethod paymentMethod) {
        validateAmount(amount);

        // Conta de origem (quem está logado)
        Customer source = customerRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada."));

        // Conta de destino (aceita UUID ou e-mail)
        String destinationKey = destinationIdentifier.trim();
        Customer destination = customerRepository.findByAccountNumberOrEmail(destinationKey)
                .orElseThrow(() -> new IllegalArgumentException("Destinatário não encontrado com o identificador: " + destinationKey));

        if (source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new IllegalArgumentException("Você não pode transferir dinheiro para a sua própria conta.");
        }

        // Debita da origem
        factory.getStrategy(paymentMethod).debit(source, amount);

        // Credita no destino
        destination.setAccountBalance(destination.getAccountBalance().add(amount));

        customerRepository.save(source);
        customerRepository.save(destination);

        // Registra no histórico com o número da conta, mesmo que o destino tenha sido informado por e-mail
        PaymentTransaction transaction = new PaymentTransaction(
                source,
                destination.getAccountNumber(),
                destinationKey,
                destination.getName(),
                amount,
                paymentMethod,
                TransactionStatus.APPROVED
        );
        transactionRepository.save(transaction);

        // Notifica os observers
        PaymentResponse response = new PaymentResponse(
                transaction.getTransactionCode(),
                TransactionStatus.APPROVED.name(),
                amount,
                transaction.getCreatedAt()
        );
        eventPublisher.publishEvent(new PaymentProcessedEvent(response, sourceAccountNumber));
        return response;
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("O valor deve ter no máximo 2 casas decimais.");
        }
    }
}
