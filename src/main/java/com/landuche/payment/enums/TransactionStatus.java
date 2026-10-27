package com.landuche.payment.enums;

public enum TransactionStatus {
    APPROVED("Aprovado"),
    REJECTED("Rejeitado"),
    FAILED("Erro"),
    PENDING("Pendente");

    private final String descricao;

    TransactionStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}