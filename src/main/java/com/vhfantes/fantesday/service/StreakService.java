package com.vhfantes.fantesday.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.StreakDTO;
import com.vhfantes.fantesday.repository.WorkoutRepository;

import lombok.RequiredArgsConstructor;

// Streak no estilo do Liftoff: dias de descanso contam, mas 3 dias seguidos sem treinar quebram a sequencia
@Service
@RequiredArgsConstructor
public class StreakService {

    // Maior distancia (em dias) entre dois treinos sem quebrar o streak: treino, 2 dias de descanso, treino
    static final int MAX_DAYS_BETWEEN_WORKOUTS = 3;

    private final WorkoutRepository workoutRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public StreakDTO getStreak(Long userId) {
        userService.getUserOrThrow(userId);
        return calculate(workoutRepository.findWorkoutDates(userId), LocalDate.now());
    }

    // dates: dias com treino, sem repeticao e em ordem crescente
    static StreakDTO calculate(List<LocalDate> dates, LocalDate today) {
        if (dates.isEmpty()) {
            return new StreakDTO(0, 0, 0, false, null, null);
        }

        LocalDate start = dates.get(0);
        LocalDate previous = start;
        int workouts = 1;
        long longest = 0;

        for (LocalDate date : dates.subList(1, dates.size())) {
            if (ChronoUnit.DAYS.between(previous, date) > MAX_DAYS_BETWEEN_WORKOUTS) {
                longest = Math.max(longest, ChronoUnit.DAYS.between(start, previous) + 1);
                start = date;
                workouts = 0;
            }
            workouts++;
            previous = date;
        }

        LocalDate last = previous;
        LocalDate expiresOn = last.plusDays(MAX_DAYS_BETWEEN_WORKOUTS);
        boolean active = !today.isAfter(expiresOn);

        // Enquanto o streak esta ativo, os dias de descanso ate hoje tambem contam
        LocalDate end = active && today.isAfter(last) ? today : last;
        long lastRunDays = ChronoUnit.DAYS.between(start, end) + 1;
        longest = Math.max(longest, active ? lastRunDays : ChronoUnit.DAYS.between(start, last) + 1);

        return new StreakDTO(
                active ? (int) lastRunDays : 0,
                (int) longest,
                active ? workouts : 0,
                active,
                last,
                active ? expiresOn : null);
    }
}
