package com.company.hr.repository;

import com.company.hr.entity.LeaveRequest;
import com.company.hr.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUserIdOrderByStartDateDesc(Long userId);

    List<LeaveRequest> findByStatusOrderByStartDateAsc(LeaveStatus status);

    List<LeaveRequest> findAllByOrderByStartDateDesc();

    @Query("select l from LeaveRequest l " +
           "where l.user.id = :userId and l.status in :statuses " +
           "and l.startDate <= :end and l.endDate >= :start")
    List<LeaveRequest> findOverlapping(@Param("userId") Long userId,
                                       @Param("statuses") Collection<LeaveStatus> statuses,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    @Query("select count(l) from LeaveRequest l " +
           "where l.user.id = :userId and l.status in :statuses " +
           "and l.startDate <= :end and l.endDate >= :start")
    long countOverlapping(@Param("userId") Long userId,
                          @Param("statuses") Collection<LeaveStatus> statuses,
                          @Param("start") LocalDate start,
                          @Param("end") LocalDate end);
}
