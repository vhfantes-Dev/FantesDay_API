package com.vhfantes.fantesday.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.vhfantes.fantesday.dto.ActivityRequestDTO;
import com.vhfantes.fantesday.dto.ActivityResponseDTO;
import com.vhfantes.fantesday.entity.Activity;
import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.repository.ActivityRepository;
import com.vhfantes.fantesday.security.CurrentUser;

@ExtendWith(MockitoExtension.class)
class ActivityServiceCaloriesTest {

    private static final long USER_ID = 3L;

    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProgressService progressService;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private ActivityService activityService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(activityService, "minutesPerXp", 2);
        ReflectionTestUtils.setField(activityService, "maxXpPerActivity", 60);
        user = new User();
        user.setId(USER_ID);
        user.setBodyweightKg(BigDecimal.valueOf(80));
    }

    @Test
    void createEstimatesCaloriesWhenNotInformed() {
        when(userService.getUserOrThrow(USER_ID)).thenReturn(user);
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivityResponseDTO result = activityService.create(USER_ID,
                new ActivityRequestDTO("Vôlei", 120, null, null, null, LocalDate.now()));

        // 4.0 x 80 kg x 2 h
        assertThat(result.caloriesBurned()).isEqualTo(640);
        assertThat(result.caloriesEstimated()).isTrue();
        assertThat(result.met()).isEqualByComparingTo("4.0");
    }

    @Test
    void createKeepsInformedCalories() {
        when(userService.getUserOrThrow(USER_ID)).thenReturn(user);
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivityResponseDTO result = activityService.create(USER_ID,
                new ActivityRequestDTO("Corrida", 30, null, null, 350, LocalDate.now()));

        assertThat(result.caloriesBurned()).isEqualTo(350);
        assertThat(result.caloriesEstimated()).isFalse();
    }

    @Test
    void unknownActivityHasNoEstimate() {
        when(userService.getUserOrThrow(USER_ID)).thenReturn(user);
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivityResponseDTO result = activityService.create(USER_ID,
                new ActivityRequestDTO("Xadrez", 60, null, null, null, LocalDate.now()));

        assertThat(result.caloriesBurned()).isNull();
        assertThat(result.caloriesEstimated()).isFalse();
    }

    @Test
    void updateRecalculatesEstimatedCaloriesWhenDurationChanges() {
        Activity activity = activity(true, 640);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        ActivityResponseDTO result = activityService.update(1L, new ActivityRequestDTO(null, 60, null, null, null, null));

        assertThat(result.caloriesBurned()).isEqualTo(320);
        assertThat(result.caloriesEstimated()).isTrue();
    }

    @Test
    void updateKeepsCaloriesInformedByTheApp() {
        Activity activity = activity(false, 500);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        ActivityResponseDTO result = activityService.update(1L, new ActivityRequestDTO(null, 60, null, null, null, null));

        assertThat(result.caloriesBurned()).isEqualTo(500);
        assertThat(result.caloriesEstimated()).isFalse();
    }

    private Activity activity(boolean estimated, int calories) {
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setUser(user);
        activity.setActivityType("Vôlei");
        activity.setMet(new BigDecimal("4.0"));
        activity.setDurationMinutes(120);
        activity.setCaloriesBurned(calories);
        activity.setCaloriesEstimated(estimated);
        activity.setXpEarned(60);
        return activity;
    }
}
