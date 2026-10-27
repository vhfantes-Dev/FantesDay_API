package com.vhfantes.fantesday.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.WorkoutSet;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {

    List<WorkoutSet> findByWorkoutExerciseIdOrderByIdAsc(Long workoutExerciseId);

    // Melhor 1RM do usuario no exercicio, ignorando uma serie (usado para detectar recorde)
    @Query("""
            select max(s.estimatedOneRepMax) from WorkoutSet s
            join s.workoutExercise we join we.workout w
            where w.user.id = :userId and we.exercise.id = :exerciseId and s.id <> :excludedSetId
            """)
    BigDecimal findBestOneRepMax(@Param("userId") Long userId, @Param("exerciseId") Long exerciseId,
            @Param("excludedSetId") Long excludedSetId);

    // Melhor 1RM de cada exercicio de um usuario
    @Query("""
            select we.exercise.id as exerciseId, max(s.estimatedOneRepMax) as bestOneRepMax from WorkoutSet s
            join s.workoutExercise we join we.workout w
            where w.user.id = :userId and s.estimatedOneRepMax is not null
            group by we.exercise.id
            """)
    List<ExerciseBest> findBestByUser(@Param("userId") Long userId);

    // Melhor 1RM de cada exercicio de todos os usuarios (usado no ranking)
    @Query("""
            select w.user.id as userId, we.exercise.id as exerciseId, max(s.estimatedOneRepMax) as bestOneRepMax
            from WorkoutSet s join s.workoutExercise we join we.workout w
            where s.estimatedOneRepMax is not null
            group by w.user.id, we.exercise.id
            """)
    List<UserExerciseBest> findBestOfAllUsers();

    // Evolucao do exercicio: melhor 1RM e volume (reps x carga) por dia de treino
    @Query("""
            select w.date as date, max(s.estimatedOneRepMax) as bestOneRepMax,
                   sum(s.reps * coalesce(s.weightKg, 0)) as volumeKg
            from WorkoutSet s join s.workoutExercise we join we.workout w
            where w.user.id = :userId and we.exercise.id = :exerciseId and w.date is not null
            group by w.date
            order by w.date
            """)
    List<ExerciseDayHistory> findHistory(@Param("userId") Long userId, @Param("exerciseId") Long exerciseId);

    // Volume levantado por usuario num periodo (ranking semanal)
    @Query("""
            select w.user.id as userId, sum(s.reps * coalesce(s.weightKg, 0)) as volumeKg,
                   count(distinct w.id) as workouts
            from WorkoutSet s join s.workoutExercise we join we.workout w
            where w.date between :start and :end
            group by w.user.id
            """)
    List<UserVolume> findVolumeBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    interface ExerciseBest {
        Long getExerciseId();

        BigDecimal getBestOneRepMax();
    }

    interface UserExerciseBest {
        Long getUserId();

        Long getExerciseId();

        BigDecimal getBestOneRepMax();
    }

    interface ExerciseDayHistory {
        LocalDate getDate();

        BigDecimal getBestOneRepMax();

        BigDecimal getVolumeKg();
    }

    interface UserVolume {
        Long getUserId();

        BigDecimal getVolumeKg();

        Long getWorkouts();
    }
}
