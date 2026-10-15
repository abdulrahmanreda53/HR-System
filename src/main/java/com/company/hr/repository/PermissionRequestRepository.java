package com.company.hr.repository;

import com.company.hr.entity.PermissionRequest;
import com.company.hr.enums.PermissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRequestRepository extends JpaRepository<PermissionRequest, Long> {
    List<PermissionRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PermissionRequest> findByStatusOrderByCreatedAtAsc(PermissionStatus status);
    List<PermissionRequest> findAllByOrderByCreatedAtDesc();
}
