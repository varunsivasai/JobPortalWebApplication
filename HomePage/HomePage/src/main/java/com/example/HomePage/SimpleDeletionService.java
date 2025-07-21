package com.example.HomePage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simple deletion service that handles foreign key constraints
 * This is the quickest fix for your current issue
 */
@Service
public class SimpleDeletionService {

    @Autowired
    private JobPostRepository jobRepo;
    
    @Autowired
    private JobApplicationRepository applicationRepo;
    
    @Autowired
    private RegisterJobRecuiterRepository recruiterRepo;

    /**
     * Delete job with its applications (manual cascade)
     * Use this method if you don't want to modify the entity relationships
     */
    @Transactional
    public boolean deleteJobWithApplications(Long jobId, String recruiterUsername) {
        // Verify recruiter ownership
        RegisterRecuiter recruiter = recruiterRepo.findByUsername(recruiterUsername);
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter not found: " + recruiterUsername);
        }
        
        JobPost job = jobRepo.findByIdAndRecruiter(jobId, recruiter).orElse(null);
        if (job == null) {
            throw new SecurityException("Access denied: You can only delete jobs you created");
        }
        
        try {
            // Step 1: Delete all applications for this job first
            applicationRepo.deleteByJobPost_Id(jobId);
            
            // Step 2: Now delete the job post
            jobRepo.delete(job);
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete job: " + e.getMessage(), e);
        }
    }
}