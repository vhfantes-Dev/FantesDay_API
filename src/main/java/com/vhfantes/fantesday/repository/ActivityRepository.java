package com.vhfantes.fantesday.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByUserId(Long userId);

    List<Activity> findByUserIdAndDate(Long userId, LocalDate date);

    boolean existsByUserIdAndImportSourceAndExternalId(Long userId, String importSource, String externalId);

    @Modifying
    @Query("delete from Activity a where a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
