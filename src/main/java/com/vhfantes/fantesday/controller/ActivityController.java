package com.vhfantes.fantesday.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhfantes.fantesday.dto.ActivityRequestDTO;
import com.vhfantes.fantesday.dto.ActivityResponseDTO;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.ActivityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final CurrentUser currentUser;

    @GetMapping("/users/{userId}/activities")
    public List<ActivityResponseDTO> findByUser(@PathVariable Long userId) {
        currentUser.checkAccess(userId);
        return activityService.findByUser(userId);
    }

    @PostMapping("/users/{userId}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponseDTO create(@PathVariable Long userId, @RequestBody ActivityRequestDTO dto) {
        currentUser.checkAccess(userId);
        return activityService.create(userId, dto);
    }

    @GetMapping("/activities/{id}")
    public ActivityResponseDTO findById(@PathVariable Long id) {
        return activityService.findById(id);
    }

    @PatchMapping("/activities/{id}")
    public ActivityResponseDTO update(@PathVariable Long id, @RequestBody ActivityRequestDTO dto) {
        return activityService.update(id, dto);
    }

    @DeleteMapping("/activities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        activityService.delete(id);
    }
}
