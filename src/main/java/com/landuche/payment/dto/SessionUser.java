package com.landuche.payment.dto;

import com.landuche.payment.domain.Customer;

// Dados guardados na sessão HTTP
public class SessionUser {

    public static final String SESSION_KEY = "loggedUser";

    private final String accountNumber;
    private final String name;

    public SessionUser(Customer customer) {
        this.accountNumber = customer.getAccountNumber();
        this.name = customer.getName();
    }

    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
}
