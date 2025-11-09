package com.school.equipment.repository;

import com.school.equipment.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    @Query("SELECT e FROM Equipment e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:availableOnly = false OR e.availableQuantity > 0) AND " +
           "(:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Equipment> findEquipmentWithFilters(@Param("category") String category,
                                           @Param("availableOnly") boolean availableOnly,
                                           @Param("search") String search);
}
