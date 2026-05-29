package com.example.backend.media;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(MediaStorageService.class);

    private final Path storageDir;
    private final String publicBaseUrl;

    public MediaStorageService(
            @Value("${media.storage-dir:./uploads}") String dir,
            @Value("${media.public-base-url:/api/media}") String publicBaseUrl
    ) {
        this.storageDir = Path.of(dir).toAbsolutePath();
        this.publicBaseUrl = publicBaseUrl;
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(storageDir);
        log.info("Media storage dir: {}", storageDir);
    }

    public String store(byte[] bytes, String extension) {
        String filename = UUID.randomUUID() + "." + extension;
        try {
            Files.write(storageDir.resolve(filename), bytes);
            log.info("Stored media: {} ({} bytes)", filename, bytes.length);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save media", e);
        }
        return publicBaseUrl + "/" + filename;
    }

    public Path resolve(String filename) {
        if (filename.contains("/") || filename.contains("..") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        return storageDir.resolve(filename);
    }
}
