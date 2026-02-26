package com.resturant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resturant.dto.SoftDrinkDTO;
import com.resturant.service.FileManagementService;
import com.resturant.service.SoftDrinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/soft-drinks")
@RequiredArgsConstructor
@Validated
public class SoftDrinkController {

    private final SoftDrinkService softDrinkService;
    @Autowired
    FileManagementService fileManagementService;

    @GetMapping
    public ResponseEntity<List<SoftDrinkDTO>> getAllSoftDrinks() {
        List<SoftDrinkDTO> softDrinks = softDrinkService.getAllSoftDrinks();
        return ResponseEntity.ok( softDrinks);
    }
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<SoftDrinkDTO> getSoftDrinkById(
            @PathVariable @Min(1) Long id) {
        SoftDrinkDTO softDrink = softDrinkService.getSoftDrinkById(id);
        return ResponseEntity.ok(softDrink);
    }

    @PostMapping(value = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SoftDrinkDTO> createSoftDrink(
            @RequestPart("drink") String rawJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException { // Changed to "image"

        ObjectMapper mapper = new ObjectMapper();
        SoftDrinkDTO dto = mapper.readValue(rawJson, SoftDrinkDTO.class);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileManagementService.saveFile(imageFile);
            dto.setImagePath(imagePath);
        }

        SoftDrinkDTO created = softDrinkService.createSoftDrink(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/admin/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SoftDrinkDTO> updateSoftDrink(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody SoftDrinkDTO softDrinkDTO) {
        SoftDrinkDTO updatedDrink = softDrinkService.updateSoftDrink(id, softDrinkDTO);
        return ResponseEntity.ok(updatedDrink);
    }

    @DeleteMapping("/admin/{id:\\d+}")

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSoftDrink(
            @PathVariable @Min(1) Long id) {
        softDrinkService.deleteSoftDrink(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/admin/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SoftDrinkDTO> updateSoftDrink(
            @PathVariable @Min(1) Long id,
            @RequestPart("drink") String rawJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        SoftDrinkDTO dto = mapper.readValue(rawJson, SoftDrinkDTO.class);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileManagementService.saveFile(imageFile);
            dto.setImagePath(imagePath);
            // Optional: Delete old image if exists
        }

        SoftDrinkDTO updatedDrink = softDrinkService.updateSoftDrink(id, dto);
        return ResponseEntity.ok(updatedDrink);
    }

    @PutMapping("/admin/{id:\\d+}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateSoftDrink(
            @PathVariable @Min(1) Long id) {
        softDrinkService.deactivateSoftDrink(id);
        return ResponseEntity.noContent().build();
    }
}
