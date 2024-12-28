package com.sgl.sglscholar.controller;

import com.sgl.sglscholar.dto.SchoolDTO;
import com.sgl.sglscholar.entity.School;
import com.sgl.sglscholar.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/schools/register")
public class RegisterSchoolController {

    @Autowired
    private SchoolService schoolService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> registerSchool(
            @RequestPart("schoolDTO") SchoolDTO schoolDTO,
            @RequestPart(value = "schoolLogo", required = false) MultipartFile schoolLogo) {
        try {
            // Handle the uploaded file (schoolLogo) if provided
            if (schoolLogo != null && !schoolLogo.isEmpty()) {
                System.out.println("Received school logo: " + schoolLogo.getOriginalFilename());
                // Add logic to upload the file to a location (e.g., S3 bucket) here if necessary.
            }

            // Register the school
            School savedSchool = schoolService.registerSchool(schoolDTO);

            // Return success message with the registered school ID
            return ResponseEntity.ok("School registered successfully with ID: " + savedSchool.getSchoolId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate entry detected: " + ex.getMessage());
    }

    // Helper method to map DTO to entity
    private School mapDtoToEntity(SchoolDTO schoolDTO) {
        School school = new School();
        school.setSchoolName(schoolDTO.getSchoolName());
        school.setCountry(schoolDTO.getCountry());
        school.setAddress(schoolDTO.getStreetAddress());
        school.setPostalCode(schoolDTO.getPostalCode());
        school.setSchoolEmail(schoolDTO.getSchoolEmail());
        school.setSchoolContact(schoolDTO.getSchoolContact());
        school.setRegistrationNumber(schoolDTO.getRegistrationNumber());
        return school;
    }

    // Helper method to map entity to DTO
    private SchoolDTO mapEntityToDto(School school) {
        SchoolDTO schoolDTO = new SchoolDTO();
        schoolDTO.setSchoolName(school.getSchoolName());
        schoolDTO.setCountry(school.getCountry());
        schoolDTO.setStreetAddress(school.getAddress());
        schoolDTO.setPostalCode(school.getPostalCode());
        schoolDTO.setSchoolEmail(school.getSchoolEmail());
        schoolDTO.setSchoolContact(school.getSchoolContact());
        schoolDTO.setRegistrationNumber(school.getRegistrationNumber());
        schoolDTO.setSchoolId(school.getSchoolId());
        return schoolDTO;
    }
}
