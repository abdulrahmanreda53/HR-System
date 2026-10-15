package com.company.hr.controller;

import com.company.hr.dto.Dtos.ManualTransactionRequest;
import com.company.hr.dto.Dtos.SalaryResponse;
import com.company.hr.dto.Dtos.TransactionResponse;
import com.company.hr.security.AppUserDetails;
import com.company.hr.service.PayrollService;
import com.company.hr.service.SalaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final SalaryService salaryService;
    private final PayrollService payrollService;


    @GetMapping("/salary/me")
    public SalaryResponse mySalary(@AuthenticationPrincipal AppUserDetails me,
                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return salaryService.calculate(me.getId(), orCurrent(month));
    }

    @GetMapping("/salary/{userId}")
    @PreAuthorize("hasRole('HR')")
    public SalaryResponse salaryFor(@PathVariable Long userId,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return salaryService.calculate(userId, orCurrent(month));
    }


    @GetMapping("/transactions/me")
    public List<TransactionResponse> myTransactions(
            @AuthenticationPrincipal AppUserDetails me,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return payrollService.listForMonth(me.getId(), orCurrent(month));
    }

    @GetMapping("/transactions/user/{userId}")
    @PreAuthorize("hasRole('HR')")
    public List<TransactionResponse> transactionsFor(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return payrollService.listForMonth(userId, orCurrent(month));
    }

    @PostMapping("/transactions")
    @PreAuthorize("hasRole('HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse addManual(@Valid @RequestBody ManualTransactionRequest req) {
        return payrollService.addManual(req);
    }

    private YearMonth orCurrent(YearMonth month) {
        return month != null ? month : YearMonth.now();
    }
}
