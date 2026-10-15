package com.company.hr.service;

import com.company.hr.entity.SystemSetting;
import com.company.hr.exception.BusinessException;
import com.company.hr.exception.ResourceNotFoundException;
import com.company.hr.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Reads dynamic configuration straight from the system_settings table (no caching, so HR edits apply immediately). */
@Service
@RequiredArgsConstructor
public class SystemSettingService {

    public static final String WORK_START_TIME = "WORK_START_TIME";
    public static final String LATE_DEDUCTION_DAYS = "LATE_DEDUCTION_DAYS";
    public static final String LATE_LONG_THRESHOLD_MINUTES = "LATE_LONG_THRESHOLD_MINUTES";
    public static final String LATE_LONG_DEDUCTION_DAYS = "LATE_LONG_DEDUCTION_DAYS";
    public static final String ADMS_CHECK_IN_CODES = "ADMS_CHECK_IN_CODES";

    private final SystemSettingRepository repository;

    @Transactional(readOnly = true)
    public String getValue(String key) {
        return repository.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElseThrow(() -> new ResourceNotFoundException("System setting not found: " + key));
    }

    @Transactional(readOnly = true)
    public LocalTime getLocalTime(String key) {
        String v = getValue(key);
        try {
            return LocalTime.parse(v.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("Setting " + key + " is not a valid HH:mm time: " + v, e);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getBigDecimal(String key) {
        String v = getValue(key);
        try {
            return new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Setting " + key + " is not a valid number: " + v, e);
        }
    }

    @Transactional(readOnly = true)
    public int getInt(String key) {
        String v = getValue(key);
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Setting " + key + " is not a valid whole number: " + v, e);
        }
    }

    @Transactional
    public void removeIfPresent(String key) {
        repository.findBySettingKey(key).ifPresent(repository::delete);
    }

    @Transactional(readOnly = true)
    public Set<Integer> getCheckInCodes() {
        return repository.findBySettingKey(ADMS_CHECK_IN_CODES)
                .map(s -> parseCodes(s.getSettingValue()))
                .orElse(Set.of(0));
    }

    @Transactional(readOnly = true)
    public List<SystemSetting> findAll() {
        return repository.findAll();
    }

    @Transactional
    public SystemSetting update(String key, String value) {
        SystemSetting setting = repository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("System setting not found: " + key));
        validate(key, value);
        setting.setSettingValue(value.trim());
        return setting;
    }

    @Transactional
    public void createIfAbsent(String key, String value, String description) {
        if (repository.findBySettingKey(key).isEmpty()) {
            repository.save(SystemSetting.builder()
                    .settingKey(key).settingValue(value).description(description).build());
        }
    }

    private void validate(String key, String value) {
        try {
            switch (key) {
                case WORK_START_TIME -> LocalTime.parse(value.trim());
                case LATE_DEDUCTION_DAYS, LATE_LONG_DEDUCTION_DAYS -> {
                    if (new BigDecimal(value.trim()).signum() < 0) {
                        throw new BusinessException(key + " must not be negative");
                    }
                }
                case LATE_LONG_THRESHOLD_MINUTES -> {
                    if (Integer.parseInt(value.trim()) < 0) {
                        throw new BusinessException(key + " must not be negative");
                    }
                }
                case ADMS_CHECK_IN_CODES -> parseCodes(value);
                default -> {  }
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new BusinessException("Invalid value for " + key + ": " + value);
        }
    }

    private Set<Integer> parseCodes(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }
}
