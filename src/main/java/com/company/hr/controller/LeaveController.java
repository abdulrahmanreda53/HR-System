package com.company.hr.controller;

import com.company.hr.dto.Dtos.LeaveBalanceResponse;
import com.company.hr.dto.Dtos.LeaveDecisionRequest;
import com.company.hr.dto.Dtos.LeaveResponse;
import com.company.hr.dto.Dtos.LeaveSubmitRequest;
import com.company.hr.enums.LeaveStatus;
import com.company.hr.security.AppUserDetails;
import com.company.hr.service.LeaveService;
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
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveResponse submit(@AuthenticationPrincipal AppUserDetails me,
                                @Valid @RequestBody LeaveSubmitRequest req) {
        return leaveService.submit(me.getId(), req);
    }

    @GetMapping("/me")
    public List<LeaveResponse> mine(@AuthenticationPrincipal AppUserDetails me) {
        return leaveService.findMine(me.getId());
    }

    @GetMapping("/balance/me")
    public LeaveBalanceResponse myBalance(
            @AuthenticationPrincipal AppUserDetails me,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return leaveService.getBalance(me.getId(), month != null ? month : YearMonth.now());
    }

    @GetMapping("/balance/{userId}")
    @PreAuthorize("hasRole('HR')")
    public LeaveBalanceResponse balanceFor(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return leaveService.getBalance(userId, month != null ? month : YearMonth.now());
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public List<LeaveResponse> all(@RequestParam(required = false) LeaveStatus status) {
        return leaveService.findAll(status);
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasRole('HR')")
    public LeaveResponse decide(@PathVariable Long id, @Valid @RequestBody LeaveDecisionRequest req) {
        return leaveService.decide(id, req);
    }
}
