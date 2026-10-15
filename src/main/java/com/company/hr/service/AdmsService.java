package com.company.hr.service;

import com.company.hr.enums.AttendanceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdmsService {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public record AdmsResult(int recorded, int duplicates, int rejected) {}
    private record ParsedPunch(String pin, LocalDateTime timestamp, int status) {}

    private final AttendanceService attendanceService;
    private final SystemSettingService settingService;

    @Value("${adms.allowed-serials:}")
    private String allowedSerialsCsv;

    public boolean isDeviceAllowed(String serialNumber) {
        if (allowedSerialsCsv == null || allowedSerialsCsv.isBlank()) {
            return true;
        }
        Set<String> allowed = Arrays.stream(allowedSerialsCsv.split(","))
                .map(String::trim).collect(Collectors.toSet());
        return allowed.contains(serialNumber);
    }

    public AdmsResult processAttLog(String serialNumber, String body) {
        if (body == null || body.isBlank()) {
            return new AdmsResult(0, 0, 0);
        }
        Set<Integer> checkInCodes = settingService.getCheckInCodes();
        int recorded = 0, duplicates = 0, rejected = 0;

        for (String rawLine : body.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            ParsedPunch punch = parseLine(line);
            if (punch == null) {
                log.warn("ADMS [{}]: unparseable line skipped: '{}'", serialNumber, line);
                rejected++;
                continue;
            }
            AttendanceType type = checkInCodes.contains(punch.status())
                    ? AttendanceType.CHECK_IN : AttendanceType.CHECK_OUT;
            try {
                switch (attendanceService.recordPunch(punch.pin(), punch.timestamp(), type)) {
                    case RECORDED -> recorded++;
                    case DUPLICATE -> duplicates++;
                    case UNKNOWN_USER, INACTIVE_USER -> rejected++;
                }
            } catch (DataIntegrityViolationException e) {
                duplicates++;
            }

        }
        log.info("ADMS [{}]: recorded={}, duplicates={}, rejected={}", serialNumber, recorded, duplicates, rejected);
        return new AdmsResult(recorded, duplicates, rejected);
    }

    private ParsedPunch parseLine(String line) {
        String[] t = line.split("\\s+");
        if (t.length < 3) return null;
        try {
            LocalDateTime ts = LocalDateTime.parse(t[1] + " " + t[2], TS_FORMAT);
            int status = t.length > 3 ? Integer.parseInt(t[3]) : 0;
            return new ParsedPunch(t[0], ts, status);
        } catch (DateTimeParseException | NumberFormatException e) {
            return null;
        }
    }
}
