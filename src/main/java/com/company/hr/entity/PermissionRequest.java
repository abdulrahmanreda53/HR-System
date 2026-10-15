package com.company.hr.entity;

import com.company.hr.enums.PermissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** A free-form permission request ("idhn") an employee sends to HR for a given date. */
@Entity
@Table(name = "permission_requests",
        indexes = @Index(name = "idx_permission_user", columnList = "user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PermissionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "permission_date", nullable = false)
    private LocalDate permissionDate;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PermissionStatus status = PermissionStatus.PENDING;

    /** HR's comment on the decision. */
    @Column(name = "hr_notes", length = 1000)
    private String hrNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
