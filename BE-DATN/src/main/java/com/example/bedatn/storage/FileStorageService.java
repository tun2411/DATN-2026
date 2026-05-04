package com.example.bedatn.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private final Path root;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String upload(MultipartFile file, String buildingId) {
        validate(file);
        try {
            Path buildingDir = root.resolve(buildingId).normalize();
            if (!buildingDir.startsWith(root)) {
                throw new IllegalArgumentException("buildingId không hợp lệ");
            }
            Files.createDirectories(buildingDir);
            String extension = extensionOf(file.getOriginalFilename());
            String storedFileName = UUID.randomUUID() + "." + extension;
            Path target = buildingDir.resolve(storedFileName).normalize();
            Files.copy(file.getInputStream(), target);
            return "/files/" + buildingId + "/" + storedFileName;
        } catch (IOException e) {
            throw new IllegalStateException("Không thể lưu file: " + e.getMessage());
        }
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        String prefix = "/files/";
        if (!fileUrl.startsWith(prefix)) {
            return;
        }
        Path target = root.resolve(fileUrl.substring(prefix.length())).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException("Không thể xóa file: " + e.getMessage());
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File vượt quá 10MB");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File không đúng định dạng. Chỉ chấp nhận PDF, JPG, JPEG, PNG");
        }
    }

    private String extensionOf(String originalName) {
        String cleanName = StringUtils.cleanPath(originalName == null ? "" : originalName);
        int dot = cleanName.lastIndexOf('.');
        if (dot < 0 || dot == cleanName.length() - 1) {
            throw new IllegalArgumentException("File không đúng định dạng. Chỉ chấp nhận PDF, JPG, JPEG, PNG");
        }
        return cleanName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
