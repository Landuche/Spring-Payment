package com.landuche.payment.service;

import com.landuche.payment.domain.Customer;
import com.landuche.payment.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Customer registerCustomer(String name, String email, String rawPassword) {
        if (isBlank(name) || isBlank(email) || isBlank(rawPassword)) {
            throw new IllegalArgumentException("Preencha todos os campos.");
        }
        if (rawPassword.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }
        if (customerRepository.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema.");
        }

        Customer customer = new Customer();
        customer.setName(name.trim());
        customer.setEmail(email.trim());
        // Criptografa a senha
        customer.setPassword(passwordEncoder.encode(rawPassword));

        // Valores para simulação
        customer.setAccountBalance(new BigDecimal("1000.00"));
        customer.setCreditLimit(new BigDecimal("2000.00"));

        return customerRepository.save(customer);
    }

    public Customer authenticate(String email, String rawPassword) {
        Customer customer = customerRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        if (!passwordEncoder.matches(rawPassword, customer.getPassword())) {
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        return customer;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
