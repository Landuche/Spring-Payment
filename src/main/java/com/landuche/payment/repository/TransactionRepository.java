package com.landuche.payment.repository;

import com.landuche.payment.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Query("SELECT t FROM PaymentTransaction t WHERE t.customer.accountNumber = :accountNumber OR t.destinationAccountNumber = :accountNumber ORDER BY t.createdAt DESC")
    List<PaymentTransaction> findTransactionsByAccount(@Param("accountNumber") String accountNumber);
}