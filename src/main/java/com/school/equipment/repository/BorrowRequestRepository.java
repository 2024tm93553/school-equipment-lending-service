package com.school.equipment.repository;

import com.school.equipment.entity.BorrowRequest;
import com.school.equipment.entity.Status;
import com.school.equipment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByRequestedBy(User user);
    List<BorrowRequest> findByStatus(Status status);

    @Query("SELECT br FROM BorrowRequest br WHERE br.equipment.equipmentId = :equipmentId " +
           "AND br.status = 'APPROVED' " +
           "AND ((br.fromDate BETWEEN :fromDate AND :toDate) " +
           "OR (br.toDate BETWEEN :fromDate AND :toDate) " +
           "OR (br.fromDate <= :fromDate AND br.toDate >= :toDate))")
    List<BorrowRequest> findOverlappingRequests(@Param("equipmentId") Long equipmentId,
                                              @Param("fromDate") LocalDate fromDate,
                                              @Param("toDate") LocalDate toDate);

    @Query("SELECT br FROM BorrowRequest br WHERE " +
           "(:status IS NULL OR br.status = :status) AND " +
           "(:userId IS NULL OR br.requestedBy.userId = :userId)")
    List<BorrowRequest> findRequestsWithFilters(@Param("status") Status status,
                                              @Param("userId") Long userId);
}
