package com.sgl.sglscholar.repository;

import com.sgl.sglscholar.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
    boolean existsBySchoolEmail(String schoolEmail); // Add this method
}
