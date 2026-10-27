package com.landuche.payment.controller;

import com.landuche.payment.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/new")
    public String createCustomerPage() {
        return "create-customer";
    }

    @PostMapping
    public String saveCustomer(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes) {
        // Delega a regra de negócio para o Service
        customerService.registerCustomer(name, email, password);
        redirectAttributes.addFlashAttribute("success", "Conta criada com sucesso! Faça login para continuar.");
        return "redirect:/";
    }
}
