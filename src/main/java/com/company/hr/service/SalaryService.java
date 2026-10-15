package com.company.hr.service;

import com.company.hr.dto.Dtos.SalaryResponse;
import com.company.hr.dto.Dtos.TransactionResponse;
import com.company.hr.entity.PayrollTransaction;
import com.company.hr.entity.User;
import com.company.hr.enums.TransactionType;
import com.company.hr.exception.ResourceNotFoundException;
import com.company.hr.repository.PayrollTransactionRepository;
import com.company.hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SalaryService {

    private final UserRepository userRepository;
    private final PayrollTransactionRepository payrollRepository;
    private final PayCalculator payCalculator;


    @Transactional(readOnly = true)
    public SalaryResponse calculate(Long userId, YearMonth month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<PayrollTransaction> all = payrollRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateAsc(
                        userId, month.atDay(1), month.atEndOfMonth());

        List<TransactionResponse> bonuses = all.stream()
                .filter(t -> t.getType() == TransactionType.BONUS)
                .map(TransactionResponse::from).toList();
        List<TransactionResponse> deductions = all.stream()
                .filter(t -> t.getType() == TransactionType.DEDUCTION)
                .map(TransactionResponse::from).toList();

        BigDecimal totalBonuses = sumAmounts(bonuses);
        BigDecimal totalDeductions = sumAmounts(deductions);
        BigDecimal deductionDays = deductions.stream()
                .map(TransactionResponse::daysFraction).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal net = user.getBaseSalary().add(totalBonuses).subtract(totalDeductions);

        return new SalaryResponse(user.getId(), user.getFullName(), month.toString(),
                user.getBaseSalary(), user.getWorkDaysPerWeek(),
                payCalculator.workingDaysInMonth(user.getWorkDaysPerWeek(), month),
                payCalculator.dailyRate(user, month),
                totalBonuses, totalDeductions, deductionDays, net, bonuses, deductions);
    }

    private BigDecimal sumAmounts(List<TransactionResponse> list) {
        return list.stream().map(TransactionResponse::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
