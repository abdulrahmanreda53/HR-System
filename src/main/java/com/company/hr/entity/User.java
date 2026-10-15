package com.company.hr.entity;

import com.company.hr.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** PIN / user id enrolled on the ZKTeco device. Unique when present. */
    @Column(name = "machine_id", unique = true, length = 50)
    private String machineId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "base_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    /** Working days per week (1-7), set by HR. Drives the daily rate. */
    @Column(name = "work_days_per_week", nullable = false)
    @ColumnDefault("5")
    @Builder.Default
    private Integer workDaysPerWeek = 5;

    /** Monthly leave allowance in days, set by HR. */
    @Column(name = "monthly_leave_days", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Integer monthlyLeaveDays = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Attendance> attendances = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<PayrollTransaction> payrollTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<LeaveRequest> leaveRequests = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<PermissionRequest> permissionRequests = new ArrayList<>();
}
