package com.company.hr.repository;

import com.company.hr.entity.Attendance;
import com.company.hr.enums.AttendanceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByUserIdAndTimestampAndType(Long userId, LocalDateTime timestamp, AttendanceType type);

    long countByUserIdAndTypeAndTimestampBetween(Long userId, AttendanceType type,
                                                 LocalDateTime from, LocalDateTime to);

    List<Attendance> findByUserIdAndTimestampBetweenOrderByTimestampAsc(Long userId,
                                                                        LocalDateTime from, LocalDateTime to);
}
