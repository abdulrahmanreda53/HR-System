package com.company.hr.repository;

import com.company.hr.entity.PayrollTransaction;
import com.company.hr.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PayrollTransactionRepository extends JpaRepository<PayrollTransaction, Long> {

    List<PayrollTransaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateAsc(
            Long userId, LocalDate start, LocalDate end);

    @Query("select sum(p.amount) from PayrollTransaction p " +
           "where p.user.id = :userId and p.type = :type " +
           "and p.transactionDate between :start and :end")
    BigDecimal sumAmount(@Param("userId") Long userId,
                         @Param("type") TransactionType type,
                         @Param("start") LocalDate start,
                         @Param("end") LocalDate end);
}
