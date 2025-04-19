package com.resturant.controller;


import com.resturant.dto.ImageDTO;
import com.resturant.service.FileManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/images")

public class FileUploadController {

    @Autowired
    FileManagementService fileManagementService;

//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFoodImage(@RequestParam("file") MultipartFile file) {
//        try {
//            String fileName = fileManagementService.saveFile(file);
//            return ResponseEntity.ok("Image uploaded successfully: " + fileName);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to upload image: " + e.getMessage());
//        }
//    }
//    @GetMapping("/{filename}")
//    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
//        try {
//            Resource fileData = fileManagementService.loadFile(filename);
//            if (fileData.exists()) {
//                // Return the file content with the appropriate media type (JPEG, PNG, etc.)
//                return ResponseEntity.ok()
//                        .contentType(getMediaType(filename))  // Dynamically set the content type based on file extension
//                        .body(fileData);
//            } else {
//                // Return 404 if file not found
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//            }
//        } catch (MalformedURLException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(null);  // Return an error response if file loading fails
//        }
//    }
//
//    @GetMapping
//    public ResponseEntity<?> getAllFileNames() {
//        try {
//            List<String> filenames = fileManagementService.listAllFiles();
//            return ResponseEntity.ok(filenames);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not list files.");
//        }
//    }
//
//    @DeleteMapping("/{filename}")
//    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
//        boolean deleted = fileManagementService.deleteFile(filename);
//        if (deleted) {
//            return ResponseEntity.ok("File deleted successfully: " + filename);
//        } else {
//            return ResponseEntity.status(404).body("File not found: " + filename);
//        }
//    }
//
//    private MediaType getMediaType(String filename) {
//        if (filename.endsWith(".png")) {
//            return MediaType.IMAGE_PNG;
//        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
//            return MediaType.IMAGE_JPEG;
//        } else {
//            return MediaType.APPLICATION_OCTET_STREAM;  // Generic binary data
//        }
//    }
@PostMapping("/upload")
public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
    try {
        String fileName = fileManagementService.saveFile(file);
        return ResponseEntity.ok("Image uploaded successfully: " + fileName);
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload image: " + e.getMessage());
    }
}

    // Get image file (returns actual image data)
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Resource file = fileManagementService.loadFile(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Adjust based on actual type
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get list of all images (metadata only)
    @GetMapping
    public ResponseEntity<List<ImageDTO>> getAllImages() {
        try {
            List<ImageDTO> fileInfos = fileManagementService.listAllFiles();
            return ResponseEntity.ok(fileInfos);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete image
    @DeleteMapping("/{filename}")
    public ResponseEntity<?> deleteFile(@PathVariable String filename) {
        try {
            // Log the deletion attempt
            log.debug("Attempting to delete file: {}", filename);

            // First verify if file exists (optional but helpful)
            if (!fileManagementService.fileExists(filename)) {
                log.warn("File not found for deletion: {}", filename);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("FILE_NOT_FOUND",
                                "File not found: " + filename, filename));
            }

            // Attempt deletion
            boolean deleted = fileManagementService.deleteFile(filename);

            if (deleted) {
                log.info("Successfully deleted file: {}", filename);
                return ResponseEntity.ok(createSuccessResponse("FILE_DELETED",
                        "File deleted successfully", filename));
            } else {
                log.warn("File deletion failed (unknown reason): {}", filename);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("DELETION_FAILED",
                                "Failed to delete file", filename));
            }

        } catch (SecurityException e) {
            log.error("Security violation while deleting {}: {}", filename, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("SECURITY_VIOLATION",
                            e.getMessage(), filename));
        } catch (IOException e) {
            log.error("IO error while deleting {}: {}", filename, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("IO_ERROR",
                            "File deletion failed: " + e.getMessage(), filename));
        }
    }


    // Helper methods for consistent responses
    private Map<String, Object> createSuccessResponse(String code, String message, String filename) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("code", code);
        response.put("message", message);
        response.put("filename", filename);
        response.put("timestamp", Instant.now());
        return response;
    }

    private Map<String, Object> createErrorResponse(String code, String message, String filename) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "error");
        response.put("code", code);
        response.put("message", message);
        response.put("filename", filename);
        response.put("timestamp", Instant.now());
        return response;
    }
}
