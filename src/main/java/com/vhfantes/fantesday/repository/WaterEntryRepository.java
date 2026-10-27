package com.vhfantes.fantesday.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.WaterEntry;

public interface WaterEntryRepository extends JpaRepository<WaterEntry, Long> {

    List<WaterEntry> findByUserIdAndDateOrderByCreatedAtAsc(Long userId, LocalDate date);

    @Query("select coalesce(sum(w.amountMl), 0) from WaterEntry w where w.user.id = :userId and w.date = :date")
    long sumByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Modifying
    @Query("delete from WaterEntry w where w.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
