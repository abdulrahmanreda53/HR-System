package com.company.hr.service;

import com.company.hr.dto.Dtos.PermissionDecisionRequest;
import com.company.hr.dto.Dtos.PermissionResponse;
import com.company.hr.dto.Dtos.PermissionSubmitRequest;
import com.company.hr.entity.PermissionRequest;
import com.company.hr.entity.User;
import com.company.hr.enums.PermissionStatus;
import com.company.hr.exception.BusinessException;
import com.company.hr.exception.ResourceNotFoundException;
import com.company.hr.repository.PermissionRequestRepository;
import com.company.hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRequestRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public PermissionResponse submit(Long userId, PermissionSubmitRequest req) {
        if (req.permissionDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Permission date must not be in the past");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        PermissionRequest saved = repository.save(PermissionRequest.builder()
                .user(user)
                .permissionDate(req.permissionDate())
                .reason(req.reason().trim())
                .status(PermissionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());
        return PermissionResponse.from(saved);
    }

    @Transactional
    public PermissionResponse decide(Long id, PermissionDecisionRequest req) {
        if (req.status() == PermissionStatus.PENDING) {
            throw new BusinessException("Decision must be APPROVED or REJECTED");
        }
        PermissionRequest request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission request not found: " + id));
        if (request.getStatus() != PermissionStatus.PENDING) {
            throw new BusinessException("Permission request has already been " + request.getStatus());
        }
        boolean noComment = req.hrNotes() == null || req.hrNotes().isBlank();
        if (req.status() == PermissionStatus.REJECTED && noComment) {
            throw new BusinessException("A comment is required when rejecting a request");
        }
        request.setStatus(req.status());
        request.setHrNotes(noComment ? null : req.hrNotes().trim());
        return PermissionResponse.from(request);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> findMine(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(PermissionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> findAll(PermissionStatus status) {
        List<PermissionRequest> list = status == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByStatusOrderByCreatedAtAsc(status);
        return list.stream().map(PermissionResponse::from).toList();
    }
}
