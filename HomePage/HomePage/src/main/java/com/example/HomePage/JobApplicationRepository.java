package com.example.HomePage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobSeeker_Id(Long seekerId);
    List<JobApplication> findByJobPost_Id(Long jobPostId);

    List<JobApplication> findByJobSeeker_IdAndJobPost_Id(Long id, Long id1);

    boolean existsByJobSeeker_IdAndJobPost_Id(Long id, Long id1);
    
    // Find applications for jobs posted by a specific recruiter
    List<JobApplication> findByJobPostIn(List<JobPost> jobPosts);
    
    // Delete all applications for a specific job (for manual cascade)
    void deleteByJobPost_Id(Long jobPostId);
}
