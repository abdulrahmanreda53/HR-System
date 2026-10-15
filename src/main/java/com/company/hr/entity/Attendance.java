package com.company.hr.entity;

import com.company.hr.enums.AttendanceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendances",
        uniqueConstraints = @UniqueConstraint(name = "uk_attendance_user_time_type",
                columnNames = {"user_id", "punch_time", "type"}),
        indexes = @Index(name = "idx_attendance_user_time", columnList = "user_id, punch_time"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "punch_time", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceType type;

    @Column(name = "is_late", nullable = false)
    @Builder.Default
    private Boolean isLate = false;

    @Column(name = "late_minutes", nullable = false)
    @Builder.Default
    private Integer lateMinutes = 0;
}
