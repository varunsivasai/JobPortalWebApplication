package com.example.HomePage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    List<JobPost> findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(String title, String company);

    List<JobPost> findByTitleContainingIgnoreCase(String search);
    
    // Find all jobs posted by a specific recruiter
    List<JobPost> findByRecruiter(RegisterRecuiter recruiter);
    
    // Find jobs posted by a specific recruiter with search functionality
    @Query("SELECT j FROM JobPost j WHERE j.recruiter = :recruiter AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<JobPost> findByRecruiterAndSearchText(@Param("recruiter") RegisterRecuiter recruiter, 
                                               @Param("search") String search);
    
    // Find a job by ID and recruiter (for authorization)
    Optional<JobPost> findByIdAndRecruiter(Long id, RegisterRecuiter recruiter);
}
