package com.vhfantes.fantesday.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.DashboardDTO;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/{userId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    // Dados da tela inicial em uma chamada; ?date=yyyy-MM-dd (padrao: hoje)
    @GetMapping
    public DashboardDTO getDashboard(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        currentUser.checkAccess(userId);
        return dashboardService.getDashboard(userId, date != null ? date : LocalDate.now());
    }
}
