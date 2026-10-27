package com.vhfantes.fantesday.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.WeightEntry;

public interface WeightEntryRepository extends JpaRepository<WeightEntry, Long> {

    Optional<WeightEntry> findByUserIdAndDate(Long userId, LocalDate date);

    Optional<WeightEntry> findFirstByUserIdOrderByDateDesc(Long userId);

    List<WeightEntry> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);

    @Modifying
    @Query("delete from WeightEntry w where w.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
