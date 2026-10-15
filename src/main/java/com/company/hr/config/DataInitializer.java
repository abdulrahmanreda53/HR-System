package com.company.hr.config;

import com.company.hr.entity.User;
import com.company.hr.enums.Role;
import com.company.hr.repository.UserRepository;
import com.company.hr.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SystemSettingService settingService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-email}")
    private String adminEmail;
    @Value("${app.bootstrap.admin-password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        settingService.createIfAbsent(SystemSettingService.WORK_START_TIME, "09:00",
                "Official work start time (HH:mm). Check-ins after this are late.");
        settingService.createIfAbsent(SystemSettingService.LATE_DEDUCTION_DAYS, "0.25",
                "Deduction for a late check-in, as a fraction of the daily wage (0.25 = quarter day).");
        settingService.createIfAbsent(SystemSettingService.LATE_LONG_THRESHOLD_MINUTES, "60",
                "A late check-in with more minutes than this uses LATE_LONG_DEDUCTION_DAYS.");
        settingService.createIfAbsent(SystemSettingService.LATE_LONG_DEDUCTION_DAYS, "0.5",
                "Deduction for a long late check-in, as a fraction of the daily wage (0.5 = half day).");
        // Replaced by the day-fraction settings above; remove the old row from existing databases.
        settingService.removeIfPresent("LATE_DEDUCTION_AMOUNT");
        settingService.createIfAbsent(SystemSettingService.ADMS_CHECK_IN_CODES, "0",
                "Comma-separated device status codes treated as CHECK_IN; all other codes are CHECK_OUT.");

        if (!userRepository.existsByRole(Role.HR)) {
            userRepository.save(User.builder()
                    .fullName("System HR Admin").email(adminEmail.toLowerCase())
                    .password(passwordEncoder.encode(adminPassword))
                    .baseSalary(BigDecimal.ZERO).role(Role.HR).jobTitle("HR Manager").isActive(true)
                    .build());
            log.warn("Bootstrap HR account created ({}). Change its password immediately.", adminEmail);
        }
    }
}
