package com.vhfantes.fantesday.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.TaskCompletion;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {

    Optional<TaskCompletion> findByTaskIdAndDate(Long taskId, LocalDate date);

    List<TaskCompletion> findByTaskId(Long taskId);

    // Conclusoes de varias tarefas (para montar a lista sem uma consulta por tarefa)
    List<TaskCompletion> findByTaskIdIn(Collection<Long> taskIds);

    @Modifying
    @Query("delete from TaskCompletion c where c.task.id in (select t.id from Task t where t.user.id = :userId)")
    void deleteByUserId(@Param("userId") Long userId);
}
