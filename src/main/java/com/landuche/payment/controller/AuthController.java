package com.landuche.payment.controller;

import com.landuche.payment.domain.Customer;
import com.landuche.payment.dto.SessionUser;
import com.landuche.payment.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final CustomerService customerService;

    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/web/auth/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletRequest request) {

        // O service valida o e-mail e confere o hash do BCrypt
        Customer customer = customerService.authenticate(email, password);

        // Descarta a sessão anterior e cria uma nova
        request.getSession().invalidate();
        request.getSession(true).setAttribute(SessionUser.SESSION_KEY, new SessionUser(customer));
        return "redirect:/";
    }

    @GetMapping("/web/auth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
