package com.vhfantes.fantesday.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.vhfantes.fantesday.entity.User;
import com.vhfantes.fantesday.entity.UserPhoto;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.repository.UserPhotoRepository;

import lombok.RequiredArgsConstructor;

// Foto de perfil guardada no Postgres (JPG, PNG ou WebP, ate 2 MB)
@Service
@RequiredArgsConstructor
public class UserPhotoService {

    public static final long MAX_SIZE_BYTES = 2 * 1024 * 1024;

    private final UserPhotoRepository userPhotoRepository;
    private final UserService userService;

    @Transactional
    public void upload(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Envie a imagem no campo 'file'");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("A imagem deve ter no maximo 2 MB");
        }

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("Nao foi possivel ler a imagem");
        }

        // O tipo vem do conteudo do arquivo, nao do nome nem do Content-Type informado pelo cliente
        String contentType = detectImageType(data);
        if (contentType == null) {
            throw new BusinessException("Formato invalido: envie JPG, PNG ou WebP");
        }

        User user = userService.getUserOrThrow(userId);
        UserPhoto photo = userPhotoRepository.findById(userId).orElseGet(() -> {
            UserPhoto created = new UserPhoto();
            created.setUser(user);
            return created;
        });
        photo.setData(data);
        photo.setContentType(contentType);
        userPhotoRepository.save(photo);

        user.setPhotoUpdatedAt(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public UserPhoto get(Long userId) {
        return userPhotoRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario sem foto de perfil"));
    }

    @Transactional
    public void delete(Long userId) {
        User user = userService.getUserOrThrow(userId);
        userPhotoRepository.findById(userId).ifPresent(userPhotoRepository::delete);
        user.setPhotoUpdatedAt(null);
    }

    // Confere a "assinatura" no inicio do arquivo
    static String detectImageType(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (data.length >= 8 && (data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G'
                && data[4] == 0x0D && data[5] == 0x0A && data[6] == 0x1A && data[7] == 0x0A) {
            return "image/png";
        }
        if (data.length >= 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
