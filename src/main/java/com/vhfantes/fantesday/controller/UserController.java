package com.vhfantes.fantesday.controller;

import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vhfantes.fantesday.dto.UserRequestDTO;
import com.vhfantes.fantesday.dto.UserResponseDTO;
import com.vhfantes.fantesday.entity.UserPhoto;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.service.AccountDeletionService;
import com.vhfantes.fantesday.service.UserPhotoService;
import com.vhfantes.fantesday.service.UserService;

import lombok.RequiredArgsConstructor;

// A criacao de conta fica em POST /api/auth/register. Cada usuario so acessa a propria conta.
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserPhotoService userPhotoService;
    private final AccountDeletionService accountDeletionService;
    private final CurrentUser currentUser;

    @GetMapping("/{id}")
    public UserResponseDTO findById(@PathVariable Long id) {
        currentUser.checkAccess(id);
        return userService.findById(id);
    }

    // Atualizacao parcial: so os campos enviados sao alterados (a senha muda em /api/auth/change-password)
    @PatchMapping("/{id}")
    public UserResponseDTO update(@PathVariable Long id, @RequestBody UserRequestDTO dto) {
        currentUser.checkAccess(id);
        return userService.update(id, dto);
    }

    // Exclui a conta e todos os dados do usuario
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        currentUser.checkAccess(id);
        accountDeletionService.deleteAccount(id);
    }

    // ---------- Foto de perfil ----------

    // multipart/form-data com o campo "file" (JPG, PNG ou WebP, ate 2 MB)
    @PutMapping(path = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDTO uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        currentUser.checkAccess(id);
        userPhotoService.upload(id, file);
        return userService.findById(id);
    }

    // Qualquer usuario logado pode ver a foto (ela aparece nos rankings)
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        UserPhoto photo = userPhotoService.get(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePrivate())
                .body(photo.getData());
    }

    @DeleteMapping("/{id}/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto(@PathVariable Long id) {
        currentUser.checkAccess(id);
        userPhotoService.delete(id);
    }
}
