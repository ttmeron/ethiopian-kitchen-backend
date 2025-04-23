package com.resturant.controller;


import com.resturant.dto.ImageDTO;
import com.resturant.dto.response.SuccessResponse;
import com.resturant.exception.ErrorResponse;
import com.resturant.service.FileManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/images")
@Tag(name = "Image Management", description = "Operations for managing food images")
public class FileUploadController {

    @Autowired
    FileManagementService fileManagementService;

    @Autowired
    HttpServletRequest request;

    @PostMapping("/upload")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(
                                    value = "\"Image uploaded successfully: doro_wot.jpg\""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<String> uploadImage(
            @Parameter(
                    description = "Image file to upload",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("file") MultipartFile file) {
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
    @Operation(
            summary = "Get image content",
            description = "Retrieve the actual image file by filename"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Image file returned",
                    content = @Content(mediaType = "image/*")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Image not found"
            )
    })
    public ResponseEntity<Resource> getImage(
            @Parameter(
                    description = "Name of the image file",
                    example = "doro_wot.jpg",
                    required = true
            )
            @PathVariable String filename) {

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
    @Operation(
            summary = "List all images",
            description = "Get metadata for all uploaded images"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of image metadata",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ImageDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(
            summary = "Delete an image",
            description = "Remove an image file from storage"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Image deleted successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Permission denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Image not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteFile(
            @Parameter(
                    description = "Name of the image file to delete",
                    example = "doro_wot.jpg",
                    required = true
            )@PathVariable String filename,
             HttpServletRequest request) {
        String requestPath = request.getRequestURI(); // This returns String

        try {
            // Log the deletion attempt
            log.debug("Attempting to delete file: {}", filename);

            // First verify if file exists (optional but helpful)
            if (!fileManagementService.fileExists(filename)) {

                log.warn("File not found for deletion: {}", filename);

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new com.resturant.dto.response.ErrorResponse(HttpStatus.NOT_FOUND,
                                "FILE_NOT_FOUND",
                                "File not found: "+ filename,
                                requestPath));
            }
            boolean deleted = fileManagementService.deleteFile(filename);
            if(!deleted) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new com.resturant.dto.response.ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "DELETION_FAILED",
                                "Failed to delte file",
                                requestPath));
            }
            return ResponseEntity.noContent().build();
        }catch (Exception e){
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new com.resturant.dto.response.ErrorResponse(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "SERVER_ERROR",
                                    "Error: "+ e.getMessage(),
                                    requestPath));

                }
            }

            // Attempt deletion
//            boolean deleted = fileManagementService.deleteFile(filename);
//
//            if (deleted) {
//                log.info("Successfully deleted file: {}", filename);
//                return ResponseEntity.ok(createSuccessResponse("FILE_DELETED",
//                        "File deleted successfully", filename));
//            } else {
//                log.warn("File deletion failed (unknown reason): {}", filename);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(createErrorResponse("DELETION_FAILED",
//                                "Failed to delete file", filename));
//            }
//
//        } catch (SecurityException e) {
//            log.error("Security violation while deleting {}: {}", filename, e.getMessage());
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(createErrorResponse("SECURITY_VIOLATION",
//                            e.getMessage(), filename));
//        } catch (IOException e) {
//            log.error("IO error while deleting {}: {}", filename, e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(createErrorResponse("IO_ERROR",
//                            "File deletion failed: " + e.getMessage(), filename));
//        }
//    }
//
//
//    // Helper methods for consistent responses
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
