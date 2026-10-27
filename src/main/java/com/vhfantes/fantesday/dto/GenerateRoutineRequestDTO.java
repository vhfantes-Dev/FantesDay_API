package com.vhfantes.fantesday.dto;

import java.util.List;

import com.vhfantes.fantesday.routine.EquipmentType;
import com.vhfantes.fantesday.routine.TrainingGoal;
import com.vhfantes.fantesday.routine.TrainingLevel;

// goal: STRENGTH | HYPERTROPHY | ENDURANCE; level: BEGINNER | INTERMEDIATE | ADVANCED;
// equipment: BARBELL, DUMBBELL, MACHINE, CABLE, BODYWEIGHT (vazio = todos);
// save: true (padrao) salva as rotinas; false so devolve a sugestao.
public record GenerateRoutineRequestDTO(
        TrainingGoal goal,
        Integer daysPerWeek,
        TrainingLevel level,
        List<EquipmentType> equipment,
        Boolean save) {
}
