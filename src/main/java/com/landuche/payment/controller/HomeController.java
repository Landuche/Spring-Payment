package com.landuche.payment.controller;

import com.landuche.payment.domain.Customer;
import com.landuche.payment.dto.SessionUser;
import com.landuche.payment.repository.CustomerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {

    private final CustomerRepository customerRepository;

    public HomeController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);

        if (sessionUser != null) {
            // Recarrega do banco para garantir saldo/limite atualizados
            Optional<Customer> customer = customerRepository.findByAccountNumber(sessionUser.getAccountNumber());
            if (customer.isPresent()) {
                model.addAttribute("loggedUser", customer.get());
            } else {
                // Conta não existe mais: encerra a sessão
                session.invalidate();
            }
        }

        model.addAttribute("customersCount", customerRepository.count());
        model.addAttribute("currentPage", "dashboard");
        return "index";
    }
}
