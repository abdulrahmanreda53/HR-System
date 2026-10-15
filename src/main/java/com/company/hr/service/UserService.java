package com.company.hr.service;

import com.company.hr.dto.Dtos.ChangePasswordRequest;
import com.company.hr.dto.Dtos.UserCreateRequest;
import com.company.hr.dto.Dtos.UserResponse;
import com.company.hr.dto.Dtos.UserUpdateRequest;
import com.company.hr.entity.User;
import com.company.hr.enums.Role;
import com.company.hr.exception.BusinessException;
import com.company.hr.exception.ResourceNotFoundException;
import com.company.hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(UserCreateRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already in use: " + email);
        }
        String machineId = blankToNull(req.machineId());
        if (machineId != null && userRepository.existsByMachineId(machineId)) {
            throw new BusinessException("Machine ID already linked to another user: " + machineId);
        }
        User user = User.builder()
                .fullName(req.fullName().trim()).email(email)
                .password(passwordEncoder.encode(req.password()))
                .baseSalary(req.baseSalary()).monthlyLeaveDays(req.monthlyLeaveDays()).workDaysPerWeek(req.workDaysPerWeek()).role(req.role())
                .jobTitle(req.jobTitle()).machineId(machineId).isActive(true)
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest req, Long actorId) {
        if (id.equals(actorId) && (Boolean.FALSE.equals(req.isActive())
                || (req.role() != null && req.role() != Role.HR))) {
            throw new BusinessException("You cannot deactivate your own account or remove your own HR role");
        }
        User user = find(id);
        if (req.fullName() != null) user.setFullName(req.fullName().trim());
        if (req.jobTitle() != null) user.setJobTitle(req.jobTitle());
        if (req.baseSalary() != null) user.setBaseSalary(req.baseSalary());
        if (req.monthlyLeaveDays() != null) user.setMonthlyLeaveDays(req.monthlyLeaveDays());
        if (req.workDaysPerWeek() != null) user.setWorkDaysPerWeek(req.workDaysPerWeek());
        if (req.role() != null) user.setRole(req.role());
        if (req.isActive() != null) user.setIsActive(req.isActive());
        if (req.machineId() != null) {
            String machineId = blankToNull(req.machineId());
            if (machineId != null && !machineId.equals(user.getMachineId())
                    && userRepository.existsByMachineId(machineId)) {
                throw new BusinessException("Machine ID already linked to another user: " + machineId);
            }
            user.setMachineId(machineId);
        }
        return UserResponse.from(user);
    }


    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        if (!req.newPassword().equals(req.confirmNewPassword())) {
            throw new BusinessException("New password and confirmation do not match");
        }
        User user = find(userId);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        if (passwordEncoder.matches(req.newPassword(), user.getPassword())) {
            throw new BusinessException("New password must be different from the current password");
        }
        user.setPassword(passwordEncoder.encode(req.newPassword())); // managed entity: flushed on commit
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return UserResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
