package com.sgl.sglscholar.service;

import com.sgl.sglscholar.dto.SchoolDTO;
import com.sgl.sglscholar.entity.School;
import com.sgl.sglscholar.repository.SchoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SchoolService {

    @Autowired
    private SchoolRepository schoolRepository;

    public School registerSchool(SchoolDTO schoolDTO) {
        // Check if a school with the same email already exists
        if (schoolRepository.existsBySchoolEmail(schoolDTO.getSchoolEmail())) {
            throw new IllegalArgumentException("School with this email already exists.");
        }

        // Convert SchoolDTO to School entity
        School school = convertToEntity(schoolDTO);

        // Generate a unique ID for the school
        String uniqueId = "SGL" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
        school.setSchoolId(uniqueId);

        // Save the school entity to the database
        return schoolRepository.save(school);
    }

    // Method to convert SchoolDTO to School entity
    private School convertToEntity(SchoolDTO schoolDTO) {
        School school = new School();
        school.setSchoolName(schoolDTO.getSchoolName());
        school.setCountry(schoolDTO.getCountry());
        school.setAddress(schoolDTO.getStreetAddress()); // Map streetAddress to address
        school.setPostalCode(schoolDTO.getPostalCode());
        school.setSchoolEmail(schoolDTO.getSchoolEmail());
        school.setSchoolContact(schoolDTO.getSchoolContact());
        school.setRegistrationNumber(schoolDTO.getRegistrationNumber());
        return school;
    }
}
