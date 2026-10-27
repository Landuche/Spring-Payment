package com.landuche.payment.domain;

import com.landuche.payment.enums.PaymentMethod;
import com.landuche.payment.enums.TransactionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String transactionCode = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    // Conta de Origem (Quem enviou o dinheiro)
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Conta de Destino (Identificador/UUID ou E-mail de quem recebeu)
    @Column(nullable = false)
    private String destinationAccountNumber;

    // Chave que o pagador digitou (e-mail ou número da conta), usada na exibição do extrato
    private String destinationKey;

    // Nome do Favorecido para exibição no extrato
    @Column(nullable = false)
    private String destinationName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PaymentTransaction() {}

    public PaymentTransaction(Customer customer, String destinationAccountNumber, String destinationKey,
                              String destinationName, BigDecimal amount, PaymentMethod method, TransactionStatus status) {
        this.customer = customer;
        this.destinationAccountNumber = destinationAccountNumber;
        this.destinationKey = destinationKey;
        this.destinationName = destinationName;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getTransactionCode() { return transactionCode; }
    public Customer getCustomer() { return customer; }
    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public String getDestinationKey() { return destinationKey; }
    public String getDestinationName() { return destinationName; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public TransactionStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}