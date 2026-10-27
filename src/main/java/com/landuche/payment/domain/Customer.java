package com.landuche.payment.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    // impede que dois pagamentos simultâneos gastem o mesmo saldo
    @Version
    private Long version;

    @Column(nullable = false, unique = true)
    private String accountNumber = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private BigDecimal accountBalance;

    @Column(nullable = false)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public BigDecimal getAccountBalance() { return accountBalance; }
    public BigDecimal getCreditLimit() { return creditLimit; }

    public void setName(String name) { this.name = name; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setAccountBalance(BigDecimal accountBalance) { this.accountBalance = accountBalance; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
}
