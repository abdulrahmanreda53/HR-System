package com.company.hr.dto;

import com.company.hr.entity.*;
import com.company.hr.enums.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public final class Dtos {
    private Dtos() {}

    // ---- Auth ----
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record LoginResponse(String token, String tokenType, UserResponse user) {}

    // ---- Users ----
    public record UserCreateRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password,
            @NotNull @PositiveOrZero BigDecimal baseSalary,
            @NotNull @PositiveOrZero @Max(31) Integer monthlyLeaveDays,
            @NotNull @Min(1) @Max(7) Integer workDaysPerWeek,
            @NotNull Role role,
            String jobTitle,
            String machineId) {}

    /** All fields optional; null = leave unchanged. An empty machineId clears the link. */
    public record UserUpdateRequest(
            String fullName,
            String jobTitle,
            @PositiveOrZero BigDecimal baseSalary,
            @PositiveOrZero @Max(31) Integer monthlyLeaveDays,
            @Min(1) @Max(7) Integer workDaysPerWeek,
            String machineId,
            Role role,
            Boolean isActive) {}

    public record UserResponse(Long id, String machineId, String fullName, String email,
                               BigDecimal baseSalary, Integer monthlyLeaveDays, Integer workDaysPerWeek, Role role,
                               String jobTitle, Boolean isActive) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getMachineId(), u.getFullName(), u.getEmail(),
                    u.getBaseSalary(), u.getMonthlyLeaveDays(), u.getWorkDaysPerWeek(), u.getRole(), u.getJobTitle(), u.getIsActive());
        }
    }

    /** Self-service password change for the logged-in user. */
    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, message = "must be at least 8 characters") String newPassword,
            @NotBlank String confirmNewPassword) {}

    // ---- Settings ----
    public record SettingUpdateRequest(@NotBlank String value) {}
    public record SettingResponse(Long id, String key, String value, String description) {
        public static SettingResponse from(SystemSetting s) {
            return new SettingResponse(s.getId(), s.getSettingKey(), s.getSettingValue(), s.getDescription());
        }
    }

    // ---- Attendance ----
    public record AttendanceResponse(Long id, Long userId, LocalDateTime timestamp, AttendanceType type,
                                     Boolean isLate, Integer lateMinutes) {
        public static AttendanceResponse from(Attendance a) {
            return new AttendanceResponse(a.getId(), a.getUser().getId(), a.getTimestamp(), a.getType(),
                    a.getIsLate(), a.getLateMinutes());
        }
    }

    public record DailyAttendanceResponse(LocalDate date, LocalDateTime firstCheckIn, LocalDateTime lastCheckOut,
                                          Long workedMinutes, Boolean isLate, Integer lateMinutes) {

    }

    // ---- Payroll ----
    public record ManualTransactionRequest(
            @NotNull Long userId,
            @Positive BigDecimal amount,
            @Positive @DecimalMax("31") BigDecimal days,
            @NotNull TransactionType type,
            @NotBlank @Size(max = 500) String reason,
            LocalDate transactionDate) {}

    public record TransactionResponse(Long id, Long userId, BigDecimal amount, BigDecimal daysFraction,
                                      TransactionType type, TransactionSource source, String reason,
                                      LocalDate transactionDate) {
        public static TransactionResponse from(PayrollTransaction t) {
            return new TransactionResponse(t.getId(), t.getUser().getId(), t.getAmount(), t.getDaysFraction(),
                    t.getType(), t.getSource(), t.getReason(), t.getTransactionDate());
        }
    }

    public record SalaryResponse(Long userId, String fullName, String month, BigDecimal baseSalary,
                                 Integer workDaysPerWeek, Integer workingDaysInMonth, BigDecimal dailyRate,
                                 BigDecimal totalBonuses, BigDecimal totalDeductions, BigDecimal deductionDays,
                                 BigDecimal netSalary,
                                 List<TransactionResponse> bonuses, List<TransactionResponse> deductions) {}

    // ---- Leave balance ----
    public record LeaveBalanceResponse(Long userId, String month, Integer monthlyAllowance,
                                       Integer takenDays, Integer pendingDays, Integer remainingDays) {}

    // ---- Permission requests ----
    public record PermissionSubmitRequest(
            @NotNull LocalDate permissionDate,
            @NotBlank @Size(max = 1000) String reason) {}

    public record PermissionDecisionRequest(@NotNull PermissionStatus status, @Size(max = 1000) String hrNotes) {}

    public record PermissionResponse(Long id, Long userId, String employeeName, LocalDate permissionDate,
                                     String reason, PermissionStatus status, String hrNotes,
                                     LocalDateTime createdAt) {
        public static PermissionResponse from(PermissionRequest p) {
            return new PermissionResponse(p.getId(), p.getUser().getId(), p.getUser().getFullName(),
                    p.getPermissionDate(), p.getReason(), p.getStatus(), p.getHrNotes(), p.getCreatedAt());
        }
    }

    // ---- Leave ----
    public record LeaveSubmitRequest(
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @NotBlank @Size(max = 1000) String reason) {}

    public record LeaveDecisionRequest(@NotNull LeaveStatus status, @Size(max = 1000) String hrNotes) {}

    public record LeaveResponse(Long id, Long userId, String employeeName, LocalDate startDate,
                                LocalDate endDate, String reason, LeaveStatus status, String hrNotes) {
        public static LeaveResponse from(LeaveRequest l) {
            return new LeaveResponse(l.getId(), l.getUser().getId(), l.getUser().getFullName(),
                    l.getStartDate(), l.getEndDate(), l.getReason(), l.getStatus(), l.getHrNotes());
        }
    }
}
