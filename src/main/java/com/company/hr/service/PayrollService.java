package com.company.hr.service;

import com.company.hr.dto.Dtos.ManualTransactionRequest;
import com.company.hr.dto.Dtos.TransactionResponse;
import com.company.hr.entity.PayrollTransaction;
import com.company.hr.entity.User;
import com.company.hr.enums.TransactionSource;
import com.company.hr.enums.TransactionType;
import com.company.hr.exception.BusinessException;
import com.company.hr.exception.ResourceNotFoundException;
import com.company.hr.repository.PayrollTransactionRepository;
import com.company.hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollTransactionRepository repository;
    private final UserRepository userRepository;
    private final PayCalculator payCalculator;

    @Transactional
    public PayrollTransaction record(User user, BigDecimal amount, BigDecimal daysFraction,
                                     TransactionType type, TransactionSource source,
                                     String reason, LocalDate date) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("Transaction amount must be greater than zero");
        }
        return repository.save(PayrollTransaction.builder()
                .user(user).amount(amount).daysFraction(daysFraction)
                .type(type).source(source).reason(reason).transactionDate(date).build());
    }


    @Transactional
    public PayrollTransaction recordByDays(User user, BigDecimal days, TransactionType type,
                                           TransactionSource source, String reason, LocalDate date) {
        BigDecimal fraction = days.setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = payCalculator.dailyRate(user, YearMonth.from(date))
                .multiply(fraction).setScale(2, RoundingMode.HALF_UP);
        return record(user, amount, fraction, type, source, reason, date);
    }

    @Transactional
    public TransactionResponse addManual(ManualTransactionRequest req) {
        boolean hasAmount = req.amount() != null;
        boolean hasDays = req.days() != null;
        if (hasAmount == hasDays) {
            throw new BusinessException("Provide either amount or days (a fraction of the daily wage), not both");
        }
        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.userId()));
        LocalDate date = req.transactionDate() != null ? req.transactionDate() : LocalDate.now();

        PayrollTransaction saved = hasDays
                ? recordByDays(user, req.days(), req.type(), TransactionSource.MANUAL, req.reason(), date)
                : record(user, req.amount(), null, req.type(), TransactionSource.MANUAL, req.reason(), date);
        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listForMonth(Long userId, YearMonth month) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        return repository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateAsc(
                        userId, month.atDay(1), month.atEndOfMonth())
                .stream().map(TransactionResponse::from).toList();
    }
}
