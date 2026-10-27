package com.landuche.payment.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Converte exceções em mensagens exibidas no layout (flash "error")
@ControllerAdvice
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    // saldo insuficiente, e-mail duplicado, login inválido
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleBusinessError(IllegalArgumentException ex, HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        return backWithError(ex.getMessage(), request, redirectAttributes);
    }

    // Valor, meio de pagamento ou campo com formato inválido
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public String handleInvalidInput(Exception ex, HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        return backWithError("Dados inválidos. Verifique os campos e tente novamente.", request, redirectAttributes);
    }

    // Duas operações tentaram alterar a mesma conta ao mesmo tempo
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public String handleConcurrency(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        return backWithError("Esta conta foi alterada por outra operação. Tente novamente.", request, redirectAttributes);
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return backWithError("Ocorreu um erro inesperado. Tente novamente.", request, redirectAttributes);
    }

    private String backWithError(String message, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", message);
        return "redirect:" + fallbackPath(request);
    }

    // Volta para a página de formulário de origem (caminho fixo, sem depender do Referer)
    private String fallbackPath(HttpServletRequest request) {
        return switch (request.getRequestURI()) {
            case "/web/payments" -> "/web/checkout";
            case "/web/customers" -> "/web/customers/new";
            default -> "/";
        };
    }
}
