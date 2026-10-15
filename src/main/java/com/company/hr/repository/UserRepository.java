package com.company.hr.repository;

import com.company.hr.entity.User;
import com.company.hr.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMachineId(String machineId);
    boolean existsByEmail(String email);
    boolean existsByMachineId(String machineId);
    boolean existsByRole(Role role);
}
