package com.landuche.payment.controller;

import com.landuche.payment.domain.PaymentTransaction;
import com.landuche.payment.dto.SessionUser;
import com.landuche.payment.enums.PaymentMethod;
import com.landuche.payment.repository.TransactionRepository;
import com.landuche.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/web")
public class PaymentController {

    private final TransactionRepository transactionRepository;
    private final PaymentService paymentService;

    public PaymentController(TransactionRepository transactionRepository, PaymentService paymentService) {
        this.transactionRepository = transactionRepository;
        this.paymentService = paymentService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        // se não estiver logado, manda pra home
        if (session.getAttribute(SessionUser.SESSION_KEY) == null) {
            return "redirect:/";
        }
        model.addAttribute("currentPage", "payments");
        model.addAttribute("currentPage", "payments");
        return "checkout";
    }

    @PostMapping("/payments")
    public String processPayment(@RequestParam String destinationAccountNumber,
                                 @RequestParam BigDecimal amount,
                                 @RequestParam PaymentMethod paymentMethod,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        SessionUser loggedUser = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (loggedUser == null) {
            return "redirect:/";
        }

        paymentService.processPayment(
                loggedUser.getAccountNumber(),
                destinationAccountNumber,
                amount,
                paymentMethod
        );

        redirectAttributes.addFlashAttribute("success", "Transferência realizada com sucesso!");
        return "redirect:/web/history/" + loggedUser.getAccountNumber();
    }

    @GetMapping("/history/{sourceAccountNumber}")
    public String historyPage(@PathVariable("sourceAccountNumber") String sourceAccountNumber,
                              HttpSession session,
                              Model model) {

        // só o dono da conta pode ver o extrato
        SessionUser loggedUser = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (loggedUser == null || !loggedUser.getAccountNumber().equals(sourceAccountNumber)) {
            return "redirect:/";
        }

        // Enviadas: a conta é a origem
        // Recebidas: o resto
        List<PaymentTransaction> all = transactionRepository.findTransactionsByAccount(sourceAccountNumber);
        List<PaymentTransaction> sent = all.stream()
                .filter(tx -> tx.getCustomer().getAccountNumber().equals(sourceAccountNumber))
                .toList();
        List<PaymentTransaction> received = all.stream()
                .filter(tx -> !tx.getCustomer().getAccountNumber().equals(sourceAccountNumber))
                .toList();

        model.addAttribute("currentPage", "history");
        model.addAttribute("sentTransactions", sent);
        model.addAttribute("receivedTransactions", received);
        return "history";
    }
}
