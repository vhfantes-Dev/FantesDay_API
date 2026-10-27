package com.vhfantes.fantesday.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.vhfantes.fantesday.config.SecurityConfig;
import com.vhfantes.fantesday.dto.TaskResponseDTO;
import com.vhfantes.fantesday.exception.ForbiddenException;
import com.vhfantes.fantesday.exception.GlobalExceptionHandler;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.TaskService;

// Sobe so a camada web (controller + seguranca + tratamento de erros), com o TaskService simulado
@WebMvcTest(TaskController.class)
@Import({ SecurityConfig.class, CurrentUser.class, GlobalExceptionHandler.class })
class TaskControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/users/7/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void ownerCanListTasksOfTheDay() throws Exception {
        LocalDate date = LocalDate.of(2026, 10, 26);
        when(taskService.findForDate(7L, date)).thenReturn(List.of(new TaskResponseDTO(1L, 7L, "Beber agua", null,
                null, null, "SEG", true, date, true, null)));

        mockMvc.perform(get("/api/users/7/tasks").param("date", "2026-10-26").with(jwt().jwt(j -> j.subject("7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Beber agua"))
                .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    void anotherUserGetsForbidden() throws Exception {
        mockMvc.perform(get("/api/users/7/tasks").with(jwt().jwt(j -> j.subject("8"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Acesso negado"));
    }

    @Test
    void forbiddenFromServiceBecomes403() throws Exception {
        when(taskService.setCompleted(eq(99L), eq(true), any())).thenThrow(new ForbiddenException("Acesso negado"));

        mockMvc.perform(patch("/api/tasks/99/complete").with(jwt().jwt(j -> j.subject("8"))))
                .andExpect(status().isForbidden());
    }
}
