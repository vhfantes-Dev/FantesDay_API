package com.vhfantes.fantesday.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.PasswordResetCode;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    // Codigo mais recente ainda nao usado
    Optional<PasswordResetCode> findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(Long userId);

    // Ao pedir um codigo novo, os anteriores deixam de valer
    @Modifying
    @Query("update PasswordResetCode c set c.usedAt = :now where c.user.id = :userId and c.usedAt is null")
    int invalidateAllByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    @Modifying
    @Query("delete from PasswordResetCode c where c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
