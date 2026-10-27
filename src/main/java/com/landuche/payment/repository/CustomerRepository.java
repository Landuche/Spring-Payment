package com.landuche.payment.repository;

import com.landuche.payment.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByAccountNumber(String accountNumber);

    Optional<Customer> findByEmail(String email);

    // Metodo pra busca hibrida
    default Optional<Customer> findByAccountNumberOrEmail(String identifier) {
        Optional<Customer> customer = findByAccountNumber(identifier);
        if (customer.isPresent()) {
            return customer;
        }
        return findByEmail(identifier);
    }
}
