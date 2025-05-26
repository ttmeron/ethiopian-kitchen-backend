package com.resturant.service;

import com.resturant.dto.ImageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
@Slf4j
@Service
public class FileManagementService {

    private final Path rootLocation;

    public FileManagementService() {
        this.rootLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String saveFile(MultipartFile file) throws IOException {
//        String originalFilename = file.getOriginalFilename();
//        String extension = originalFilename != null ?
//                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
//        String filename = UUID.randomUUID() + extension;
//
//        Path destinationFile = this.rootLocation.resolve(filename)
//                .normalize()
//                .toAbsolutePath();
//
//        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
//        return filename;
        // Validate file is not empty
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        // Validate filename
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFilename.contains("..")) {
            throw new IOException("Cannot store file with relative path: " + originalFilename);
        }

        // Validate file extension
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!Arrays.asList(".jpg", ".jpeg", ".png", ".gif").contains(extension)) {
            throw new IOException("Invalid image file format");
        }

        // Generate secure filename
        String filename = UUID.randomUUID().toString() + extension;
        Path destinationFile = this.rootLocation.resolve(filename)
                .normalize()
                .toAbsolutePath();

        // Verify destination directory exists
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }

        // Verify not outside root location
        if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
            throw new IOException("Cannot store file outside target directory");
        }

        // Save file with overwrite protection
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return filename;
    }

    public Resource loadFile(String filename) throws IOException {
        Path file = rootLocation.resolve(filename).normalize();
        Resource resource = new UrlResource(file.toUri());

        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("Could not read file: " + filename);
        }
    }

    public List<ImageDTO> listAllFiles() throws IOException {
        return Files.walk(this.rootLocation, 1)
                .filter(path -> !path.equals(this.rootLocation))
                .filter(Files::isRegularFile)
                .map(path -> {
                    try {
                        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                        return new ImageDTO(
                                path.getFileName().toString(),
                                "/api/images/" + path.getFileName().toString(),
                                attrs.size(),
                                Files.probeContentType(path),
                                LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault())
                        );
                    } catch (IOException e) {
                        throw new RuntimeException("Error reading file attributes", e);
                    }
                })
                .collect(Collectors.toList());
    }

    public boolean deleteFile(String filename) throws IOException {
        Path filePath = rootLocation.resolve(filename).normalize();

        // Additional security check to prevent directory traversal
        if (!filePath.startsWith(rootLocation)) {
            throw new SecurityException("Cannot delete files outside upload directory");
        }

        return Files.deleteIfExists(filePath);
    }
    private Path resolveSafePath(String filename) throws IOException {
        if (filename == null || filename.contains("..")) {
            throw new SecurityException("Invalid filename");
        }

        Path filePath = rootLocation.resolve(filename).normalize();

        // Security check
        if (!filePath.startsWith(rootLocation)) {
            log.error("Path traversal attempt detected: {}", filename);
            throw new SecurityException("Cannot access files outside upload directory");
        }

        return filePath;
    }

    public boolean fileExists(String filename) throws IOException {
        Path filePath = resolveSafePath(filename);
        return Files.exists(filePath);
    }
}
