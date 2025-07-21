package com.example.HomePage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Alternative service for soft delete functionality
 * Use this if you prefer to mark jobs as inactive instead of physical deletion
 */
@Service
public class SoftDeleteJobService {

    @Autowired
    private JobPostRepository jobRepo;
    
    @Autowired
    private RegisterJobRecuiterRepository recruiterRepo;

    /**
     * Soft delete a job (mark as inactive)
     */
    public boolean softDeleteJobForRecruiter(Long id, String recruiterUsername) {
        RegisterRecuiter recruiter = recruiterRepo.findByUsername(recruiterUsername);
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter not found: " + recruiterUsername);
        }
        
        JobPost job = jobRepo.findByIdAndRecruiter(id, recruiter).orElse(null);
        if (job == null) {
            throw new SecurityException("Access denied: You can only delete jobs you created");
        }
        
        // Mark as inactive instead of deleting
        // Note: You would need to add an 'active' field to JobPost entity for this to work
        // job.setActive(false);
        // jobRepo.save(job);
        
        return true;
    }

    /**
     * Get active jobs only (for soft delete implementation)
     */
    public List<JobPost> getActiveJobsByRecruiter(String recruiterUsername) {
        RegisterRecuiter recruiter = recruiterRepo.findByUsername(recruiterUsername);
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter not found: " + recruiterUsername);
        }
        
        return jobRepo.findByRecruiter(recruiter).stream()
            // .filter(job -> job.isActive()) // Uncomment if implementing soft delete
            .collect(Collectors.toList());
    }
}