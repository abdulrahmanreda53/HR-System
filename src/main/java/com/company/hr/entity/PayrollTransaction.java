package com.company.hr.entity;

import com.company.hr.enums.TransactionSource;
import com.company.hr.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payroll_transactions",
        indexes = @Index(name = "idx_payroll_user_date", columnList = "user_id, transaction_date"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Set when the amount was computed as a fraction of the daily wage (0.25 = quarter day). */
    @Column(name = "days_fraction", precision = 5, scale = 2)
    private BigDecimal daysFraction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionSource source;

    @Column(length = 500)
    private String reason;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
}
