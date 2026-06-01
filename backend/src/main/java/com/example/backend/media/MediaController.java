package com.example.backend.media;

import com.example.backend.auth.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private static final long MAX_SIZE = 8L * 1024 * 1024;   // 8MB

    private final MediaStorageService storage;

    public MediaController(MediaStorageService storage) {
        this.storage = storage;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        requireLogin(session);
        if (file.isEmpty()) throw new IllegalArgumentException("파일이 비어있습니다.");
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("파일 용량은 8MB 이하만 가능합니다.");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        String extension = extractExtension(file.getOriginalFilename(), contentType);
        String url = storage.store(file.getBytes(), extension);
        return Map.of("url", url);
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serve(@PathVariable String filename) {
        Path file;
        try {
            file = storage.resolve(filename);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(contentTypeFor(filename))
                .header("Cache-Control", "public, max-age=86400")
                .body(new FileSystemResource(file));
    }

    private MediaType contentTypeFor(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.valueOf("image/webp");
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private String extractExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            if (ext.matches("png|jpg|jpeg|gif|webp")) return ext;
        }
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "bin";
        };
    }

    private void requireLogin(HttpSession session) {
        if (session.getAttribute(SessionUser.SESSION_KEY) == null) {
            throw new SecurityException("로그인이 필요합니다.");
        }
    }
}
