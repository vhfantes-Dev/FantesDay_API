package com.vhfantes.fantesday.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhfantes.fantesday.dto.ExerciseHistoryDTO;
import com.vhfantes.fantesday.dto.ExerciseRankDTO;
import com.vhfantes.fantesday.dto.LeaderboardEntryDTO;
import com.vhfantes.fantesday.dto.MuscleRankDTO;
import com.vhfantes.fantesday.dto.RankDTO;
import com.vhfantes.fantesday.dto.StrengthProfileDTO;
import com.vhfantes.fantesday.dto.WeeklyLeaderboardEntryDTO;
import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.repository.ExerciseRepository;
import com.vhfantes.fantesday.repository.UserRepository;
import com.vhfantes.fantesday.repository.WorkoutSetRepository;
import com.vhfantes.fantesday.strength.StrengthCalculator;
import com.vhfantes.fantesday.strength.StrengthStandard;
import com.vhfantes.fantesday.strength.StrengthStandards;

import lombok.RequiredArgsConstructor;

// Rank de forca no estilo do Liftoff:
// 1. cada serie estima o 1RM (Epley);
// 2. o melhor 1RM de cada exercicio com rank, dividido pelo peso corporal, vira uma nota de 1 a 1000;
// 3. as notas dos exercicios formam a nota de cada grupo muscular, e as dos grupos formam a nota geral.
@Service
@RequiredArgsConstructor
public class StrengthService {

    private static final String NO_MUSCLE_GROUP = "Outros";

    private final WorkoutSetRepository workoutSetRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ExerciseService exerciseService;
    private final StrengthStandards standards;

    @Transactional(readOnly = true)
    public StrengthProfileDTO getProfile(Long userId) {
        User user = userService.getUserOrThrow(userId);
        if (!hasBodyInfo(user)) {
            throw new BusinessException("Informe o peso corporal e o sexo do usuario para calcular o rank");
        }

        Map<Long, BigDecimal> bests = workoutSetRepository.findBestByUser(userId).stream()
                .collect(Collectors.toMap(WorkoutSetRepository.ExerciseBest::getExerciseId,
                        WorkoutSetRepository.ExerciseBest::getBestOneRepMax));
        Map<Long, Exercise> exercises = exercisesById(bests.keySet());

        List<ExerciseRankDTO> exerciseRanks = rankExercises(user, bests, exercises);
        List<MuscleRankDTO> muscleRanks = rankMuscles(exerciseRanks);
        RankDTO overall = muscleRanks.isEmpty() ? null : RankDTO.of(overallScore(muscleRanks));

        return new StrengthProfileDTO(user.getId(), user.getBodyweightKg(), user.getSex(), overall, muscleRanks, exerciseRanks);
    }

    // Rank atual do usuario em um exercicio (null se o exercicio nao tem rank, se nao ha series
    // ou se faltam peso corporal e sexo)
    @Transactional(readOnly = true)
    public ExerciseRankDTO rankForExercise(User user, Exercise exercise) {
        if (!hasBodyInfo(user)) {
            return null;
        }
        return standards.find(exercise.getExternalId())
                .map(standard -> {
                    BigDecimal best = workoutSetRepository.findBestOneRepMax(user.getId(), exercise.getId(), -1L);
                    return best == null ? null : toRank(user, exercise, standard, best);
                })
                .orElse(null);
    }

    // Ranking geral pela nota de forca. Usuarios sem peso corporal/sexo ou sem exercicio com rank ficam de fora.
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> leaderboard(int limit) {
        Map<Long, Map<Long, BigDecimal>> bestsByUser = new HashMap<>();
        for (WorkoutSetRepository.UserExerciseBest row : workoutSetRepository.findBestOfAllUsers()) {
            bestsByUser.computeIfAbsent(row.getUserId(), id -> new HashMap<>())
                    .put(row.getExerciseId(), row.getBestOneRepMax());
        }

        Map<Long, Exercise> exercises = exercisesById(bestsByUser.values().stream()
                .flatMap(bests -> bests.keySet().stream())
                .collect(Collectors.toSet()));

        record Scored(User user, int score) {
        }
        List<Scored> scored = new ArrayList<>();
        for (User user : userRepository.findAllById(bestsByUser.keySet())) {
            if (!hasBodyInfo(user)) {
                continue;
            }
            List<MuscleRankDTO> muscles = rankMuscles(rankExercises(user, bestsByUser.get(user.getId()), exercises));
            if (!muscles.isEmpty()) {
                scored.add(new Scored(user, overallScore(muscles)));
            }
        }

        scored.sort(Comparator.comparingInt(Scored::score).reversed());

        List<LeaderboardEntryDTO> entries = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            Scored entry = scored.get(i);
            entries.add(new LeaderboardEntryDTO(i + 1, entry.user().getId(), entry.user().getName(),
                    entry.score(), RankDTO.of(entry.score()).rank()));
        }
        return entries;
    }

    // Ranking da semana atual (segunda a domingo) pelo volume levantado
    @Transactional(readOnly = true)
    public List<WeeklyLeaderboardEntryDTO> weeklyLeaderboard(int limit) {
        LocalDate start = LocalDate.now().with(DayOfWeek.MONDAY);
        List<WorkoutSetRepository.UserVolume> volumes = new ArrayList<>(
                workoutSetRepository.findVolumeBetween(start, start.plusDays(6)));
        volumes.sort(Comparator.comparing(WorkoutSetRepository.UserVolume::getVolumeKg).reversed());

        Map<Long, User> users = userRepository.findAllById(volumes.stream().map(WorkoutSetRepository.UserVolume::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<WeeklyLeaderboardEntryDTO> entries = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, volumes.size()); i++) {
            WorkoutSetRepository.UserVolume volume = volumes.get(i);
            User user = users.get(volume.getUserId());
            entries.add(new WeeklyLeaderboardEntryDTO(i + 1, volume.getUserId(), user != null ? user.getName() : null,
                    volume.getVolumeKg().setScale(2, RoundingMode.HALF_UP), volume.getWorkouts()));
        }
        return entries;
    }

    // Evolucao do 1RM estimado e do volume de um exercicio, dia a dia
    @Transactional(readOnly = true)
    public List<ExerciseHistoryDTO> history(Long userId, Long exerciseId) {
        userService.getUserOrThrow(userId);
        exerciseService.getExerciseOrThrow(exerciseId);
        return workoutSetRepository.findHistory(userId, exerciseId).stream()
                .map(day -> new ExerciseHistoryDTO(day.getDate(), day.getBestOneRepMax(),
                        day.getVolumeKg() != null ? day.getVolumeKg().setScale(2, RoundingMode.HALF_UP) : null))
                .toList();
    }

    private List<ExerciseRankDTO> rankExercises(User user, Map<Long, BigDecimal> bests, Map<Long, Exercise> exercises) {
        List<ExerciseRankDTO> ranks = new ArrayList<>();
        bests.forEach((exerciseId, best) -> {
            Exercise exercise = exercises.get(exerciseId);
            if (exercise != null) {
                standards.find(exercise.getExternalId())
                        .ifPresent(standard -> ranks.add(toRank(user, exercise, standard, best)));
            }
        });
        ranks.sort(Comparator.comparingInt((ExerciseRankDTO rank) -> rank.rank().score()).reversed());
        return ranks;
    }

    private List<MuscleRankDTO> rankMuscles(List<ExerciseRankDTO> exerciseRanks) {
        Map<String, List<Integer>> scoresByMuscle = new HashMap<>();
        for (ExerciseRankDTO rank : exerciseRanks) {
            String muscle = rank.muscleGroup() != null ? rank.muscleGroup() : NO_MUSCLE_GROUP;
            scoresByMuscle.computeIfAbsent(muscle, key -> new ArrayList<>()).add(rank.rank().score());
        }

        return scoresByMuscle.entrySet().stream()
                .map(entry -> new MuscleRankDTO(entry.getKey(), entry.getValue().size(),
                        RankDTO.of(StrengthCalculator.aggregate(entry.getValue()))))
                .sorted(Comparator.comparingInt((MuscleRankDTO rank) -> rank.rank().score()).reversed())
                .toList();
    }

    private int overallScore(List<MuscleRankDTO> muscleRanks) {
        return StrengthCalculator.aggregate(muscleRanks.stream().map(rank -> rank.rank().score()).toList());
    }

    private ExerciseRankDTO toRank(User user, Exercise exercise, StrengthStandard standard, BigDecimal bestOneRepMax) {
        BigDecimal ratio = bestOneRepMax.divide(user.getBodyweightKg(), 4, RoundingMode.HALF_UP);
        int score = StrengthCalculator.score(ratio.doubleValue(), standard.anchorsFor(user.getSex()));
        return new ExerciseRankDTO(exercise.getId(), exercise.getName(), exercise.getMuscleGroup(),
                bestOneRepMax, ratio.setScale(2, RoundingMode.HALF_UP), RankDTO.of(score));
    }

    private Map<Long, Exercise> exercisesById(Iterable<Long> ids) {
        return exerciseRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Exercise::getId, Function.identity()));
    }

    private boolean hasBodyInfo(User user) {
        return user.getBodyweightKg() != null && user.getBodyweightKg().signum() > 0 && user.getSex() != null;
    }
}
