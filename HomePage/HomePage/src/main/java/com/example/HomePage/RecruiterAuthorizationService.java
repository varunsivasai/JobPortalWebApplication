package com.example.HomePage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Centralized authorization service for recruiter-related operations
 */
@Service
public class RecruiterAuthorizationService {

    @Autowired
    private RegisterJobRecuiterRepository recruiterRepo;
    
    @Autowired
    private JobPostRepository jobRepo;

    /**
     * Check if a recruiter can access/modify a specific job post
     */
    public boolean canAccessJob(String recruiterUsername, Long jobId) {
        if (recruiterUsername == null || jobId == null) {
            return false;
        }
        
        try {
            RegisterRecuiter recruiter = recruiterRepo.findByUsername(recruiterUsername);
            if (recruiter == null) {
                return false;
            }
            
            return jobRepo.findByIdAndRecruiter(jobId, recruiter).isPresent();
        } catch (Exception e) {
            // Log error and deny access on any exception
            System.err.println("Authorization check failed for user " + recruiterUsername + 
                             " and job " + jobId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Get recruiter entity by username
     */
    public RegisterRecuiter getRecruiter(String username) {
        return recruiterRepo.findByUsername(username);
    }

    /**
     * Check if username corresponds to a valid recruiter
     */
    public boolean isValidRecruiter(String username) {
        return username != null && recruiterRepo.findByUsername(username) != null;
    }

    /**
     * Security check for session-based authentication
     */
    public boolean isRecruiterLoggedIn(String sessionUsername, String sessionRole) {
        return "recruiter".equals(sessionRole) && 
               sessionUsername != null && 
               isValidRecruiter(sessionUsername);
    }
}