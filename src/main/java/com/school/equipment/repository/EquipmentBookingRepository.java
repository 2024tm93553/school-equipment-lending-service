package com.school.equipment.repository;

import com.school.equipment.entity.EquipmentBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentBookingRepository extends JpaRepository<EquipmentBooking, Long> {

    @Query("SELECT SUM(eb.quantity) FROM EquipmentBooking eb WHERE eb.equipment.equipmentId = :equipmentId " +
            "AND eb.bookingDate = :date AND eb.status = 'ACTIVE'")
    Integer getTotalBookedQuantityForDate(@Param("equipmentId") Long equipmentId,
                                          @Param("date") LocalDate date);

    List<EquipmentBooking> findByBorrowRequestRequestId(Long requestId);
}
