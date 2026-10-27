package com.landuche.payment.service;

import com.landuche.payment.domain.Customer;
import com.landuche.payment.domain.PaymentTransaction;
import com.landuche.payment.enums.PaymentMethod;
import com.landuche.payment.factory.PaymentStrategyFactory;
import com.landuche.payment.observer.PaymentProcessedEvent;
import com.landuche.payment.repository.CustomerRepository;
import com.landuche.payment.repository.TransactionRepository;
import com.landuche.payment.strategy.CreditCardPaymentStrategy;
import com.landuche.payment.strategy.PixPaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private PaymentService paymentService;

    private Customer sourceCustomer;
    private Customer destinationCustomer;

    @BeforeEach
    void setUp() {
        // Usa as strategies para testar o débito de verdade
        var factory = new PaymentStrategyFactory(List.of(new PixPaymentStrategy(), new CreditCardPaymentStrategy()));
        paymentService = new PaymentService(factory, eventPublisher, customerRepository, transactionRepository);

        sourceCustomer = new Customer();
        sourceCustomer.setAccountNumber("ACC-ORIGIN-001");
        sourceCustomer.setAccountBalance(new BigDecimal("1000.00"));
        sourceCustomer.setCreditLimit(new BigDecimal("500.00"));

        destinationCustomer = new Customer();
        destinationCustomer.setAccountNumber("ACC-DEST-002");
        destinationCustomer.setEmail("destinatario@email.com");
        destinationCustomer.setName("Gorivaldo Silva");
        destinationCustomer.setAccountBalance(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Deve processar transferência via PIX com sucesso entre contas")
    void shouldProcessPixTransferSuccessfully() {
        String destinationIdentifier = "destinatario@email.com";
        BigDecimal transferAmount = new BigDecimal("250.00");

        when(customerRepository.findByAccountNumber(sourceCustomer.getAccountNumber()))
                .thenReturn(Optional.of(sourceCustomer));
        when(customerRepository.findByAccountNumberOrEmail(destinationIdentifier))
                .thenReturn(Optional.of(destinationCustomer));

        paymentService.processPayment(sourceCustomer.getAccountNumber(), destinationIdentifier, transferAmount, PaymentMethod.PIX);

        assertEquals(0, new BigDecimal("750.00").compareTo(sourceCustomer.getAccountBalance()));
        assertEquals(0, new BigDecimal("350.00").compareTo(destinationCustomer.getAccountBalance()));

        verify(customerRepository, times(1)).save(sourceCustomer);
        verify(customerRepository, times(1)).save(destinationCustomer);

        ArgumentCaptor<PaymentTransaction> txCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());

        PaymentTransaction savedTx = txCaptor.getValue();
        assertEquals(transferAmount, savedTx.getAmount());
        assertEquals(PaymentMethod.PIX, savedTx.getMethod());
        assertEquals("Gorivaldo Silva", savedTx.getDestinationName());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar transferir com saldo insuficiente em conta corrente")
    void shouldThrowExceptionWhenInsufficientBalance() {
        when(customerRepository.findByAccountNumber(sourceCustomer.getAccountNumber()))
                .thenReturn(Optional.of(sourceCustomer));
        when(customerRepository.findByAccountNumberOrEmail(any()))
                .thenReturn(Optional.of(destinationCustomer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(sourceCustomer.getAccountNumber(), "destinatario@email.com",
                        new BigDecimal("1500.00"), PaymentMethod.PIX)
        );

        assertEquals("Saldo insuficiente em conta corrente.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve impedir transferência para a própria conta")
    void shouldPreventTransferToSelf() {
        when(customerRepository.findByAccountNumber(sourceCustomer.getAccountNumber()))
                .thenReturn(Optional.of(sourceCustomer));
        when(customerRepository.findByAccountNumberOrEmail(sourceCustomer.getAccountNumber()))
                .thenReturn(Optional.of(sourceCustomer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(sourceCustomer.getAccountNumber(), sourceCustomer.getAccountNumber(),
                        new BigDecimal("100.00"), PaymentMethod.PIX)
        );

        assertEquals("Você não pode transferir dinheiro para a sua própria conta.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar valor negativo sem alterar saldos")
    void shouldRejectNegativeAmount() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(sourceCustomer.getAccountNumber(), "destinatario@email.com",
                        new BigDecimal("-500.00"), PaymentMethod.PIX)
        );

        assertEquals("O valor da transferência deve ser maior que zero.", exception.getMessage());
        assertEquals(0, new BigDecimal("1000.00").compareTo(sourceCustomer.getAccountBalance()));
        assertEquals(0, new BigDecimal("100.00").compareTo(destinationCustomer.getAccountBalance()));
        verifyNoInteractions(customerRepository, transactionRepository, eventPublisher);
    }

    @Test
    @DisplayName("Deve rejeitar valor zero")
    void shouldRejectZeroAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(sourceCustomer.getAccountNumber(), "destinatario@email.com",
                        BigDecimal.ZERO, PaymentMethod.PIX)
        );
        verifyNoInteractions(customerRepository, transactionRepository);
    }

    @Test
    @DisplayName("Deve rejeitar valor com mais de 2 casas decimais")
    void shouldRejectAmountWithMoreThanTwoDecimals() {
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(sourceCustomer.getAccountNumber(), "destinatario@email.com",
                        new BigDecimal("10.001"), PaymentMethod.PIX)
        );
        verifyNoInteractions(customerRepository, transactionRepository);
    }

    @Test
    @DisplayName("Deve registrar o número da conta do destino mesmo quando informado por e-mail")
    void shouldStoreDestinationAccountNumberWhenIdentifiedByEmail() {
        when(customerRepository.findByAccountNumber(sourceCustomer.getAccountNumber()))
                .thenReturn(Optional.of(sourceCustomer));
        when(customerRepository.findByAccountNumberOrEmail("destinatario@email.com"))
                .thenReturn(Optional.of(destinationCustomer));

        paymentService.processPayment(sourceCustomer.getAccountNumber(), "destinatario@email.com",
                new BigDecimal("10.00"), PaymentMethod.PIX);

        ArgumentCaptor<PaymentTransaction> txCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertEquals("ACC-DEST-002", txCaptor.getValue().getDestinationAccountNumber());
        assertEquals("destinatario@email.com", txCaptor.getValue().getDestinationKey());
    }

    @Test
    @DisplayName("Deve debitar do limite de crédito (e não do saldo) no cartão")
    void shouldDebitCreditLimitOnCreditCard() {
        when(customerRepository.findByAccountNumber(sourceCustomer.getAccountNumber()))
                .thenReturn(Optional.of(sourceCustomer));
        when(customerRepository.findByAccountNumberOrEmail(any()))
                .thenReturn(Optional.of(destinationCustomer));

        paymentService.processPayment(sourceCustomer.getAccountNumber(), "destinatario@email.com",
                new BigDecimal("200.00"), PaymentMethod.CREDIT_CARD);

        assertEquals(0, new BigDecimal("300.00").compareTo(sourceCustomer.getCreditLimit()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(sourceCustomer.getAccountBalance()));
        assertEquals(0, new BigDecimal("300.00").compareTo(destinationCustomer.getAccountBalance()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o limite de crédito for insuficiente")
    void shouldThrowExceptionWhenInsufficientCreditLimit() {
        when(customerRepository.findByAccountNumber(sourceCustomer.getAccountNumber()))
                .thenReturn(Optional.of(sourceCustomer));
        when(customerRepository.findByAccountNumberOrEmail(any()))
                .thenReturn(Optional.of(destinationCustomer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(sourceCustomer.getAccountNumber(), "destinatario@email.com",
                        new BigDecimal("600.00"), PaymentMethod.CREDIT_CARD)
        );

        assertEquals("Limite de crédito insuficiente.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o destinatário não existir")
    void shouldThrowExceptionWhenDestinationNotFound() {
        when(customerRepository.findByAccountNumber(sourceCustomer.getAccountNumber()))
                .thenReturn(Optional.of(sourceCustomer));
        when(customerRepository.findByAccountNumberOrEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(sourceCustomer.getAccountNumber(), "ninguem@email.com",
                        new BigDecimal("10.00"), PaymentMethod.PIX)
        );
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve publicar evento de pagamento processado após sucesso")
    void shouldPublishEventOnSuccess() {
        when(customerRepository.findByAccountNumber(sourceCustomer.getAccountNumber()))
                .thenReturn(Optional.of(sourceCustomer));
        when(customerRepository.findByAccountNumberOrEmail(any()))
                .thenReturn(Optional.of(destinationCustomer));

        paymentService.processPayment(sourceCustomer.getAccountNumber(), "destinatario@email.com",
                new BigDecimal("10.00"), PaymentMethod.PIX);

        verify(eventPublisher).publishEvent(any(PaymentProcessedEvent.class));
    }
}
