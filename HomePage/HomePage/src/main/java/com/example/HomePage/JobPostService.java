package com.example.HomePage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JobPostService {

    @Autowired
    private JobPostRepository jobRepo;
    
    @Autowired
    private RegisterJobRecuiterRepository recruiterRepo;

    // Public methods (for job seekers and general listing)
    public List<JobPost> getAllJobs() {
        return jobRepo.findAll();
    }

    public JobPost getJobById(Long id) {
        return jobRepo.findById(id).orElse(null);
    }

    // Search jobs by title or company (public method)
    public List<JobPost> searchJobs(String search) {
        if (search == null || search.trim().isEmpty()) {
            return jobRepo.findAll();
        }
        return jobRepo.findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(search, search);
    }

    // Recruiter-specific methods (with ownership enforcement)
    
    /**
     * Get all jobs posted by a specific recruiter
     */
    public List<JobPost> getJobsByRecruiter(String recruiterUsername) {
        RegisterRecuiter recruiter = recruiterRepo.findByUsername(recruiterUsername);
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter not found: " + recruiterUsername);
        }
        
        List<JobPost> jobs = jobRepo.findByRecruiter(recruiter);
        
        // Filter out any jobs that might have null recruiters (edge case during migration)
        return jobs.stream()
            .filter(job -> job.getRecruiter() != null)
            .toList();
    }
    
    /**
     * Search jobs posted by a specific recruiter
     */
    public List<JobPost> searchJobsByRecruiter(String recruiterUsername, String search) {
        RegisterRecuiter recruiter = recruiterRepo.findByUsername(recruiterUsername);
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter not found: " + recruiterUsername);
        }
        
        if (search == null || search.trim().isEmpty()) {
            return jobRepo.findByRecruiter(recruiter);
        }
        return jobRepo.findByRecruiterAndSearchText(recruiter, search);
    }
    
    /**
     * Get a job by ID, but only if it belongs to the specified recruiter
     */
    public JobPost getJobByIdAndRecruiter(Long id, String recruiterUsername) {
        RegisterRecuiter recruiter = recruiterRepo.findByUsername(recruiterUsername);
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter not found: " + recruiterUsername);
        }
        return jobRepo.findByIdAndRecruiter(id, recruiter).orElse(null);
    }
    
    /**
     * Save a job with recruiter ownership
     */
    public JobPost saveJobForRecruiter(JobPost job, String recruiterUsername) {
        RegisterRecuiter recruiter = recruiterRepo.findByUsername(recruiterUsername);
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter not found: " + recruiterUsername);
        }
        
        // Set the recruiter for new jobs
        if (job.getId() == null) {
            job.setRecruiter(recruiter);
        } else {
            // For updates, verify ownership
            JobPost existingJob = jobRepo.findByIdAndRecruiter(job.getId(), recruiter).orElse(null);
            if (existingJob == null) {
                throw new SecurityException("Access denied: You can only edit jobs you created");
            }
            job.setRecruiter(recruiter); // Ensure recruiter is set
        }
        
        return jobRepo.save(job);
    }
    
    /**
     * Delete a job, but only if it belongs to the specified recruiter
     */
    public boolean deleteJobForRecruiter(Long id, String recruiterUsername) {
        RegisterRecuiter recruiter = recruiterRepo.findByUsername(recruiterUsername);
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter not found: " + recruiterUsername);
        }
        
        JobPost job = jobRepo.findByIdAndRecruiter(id, recruiter).orElse(null);
        if (job == null) {
            throw new SecurityException("Access denied: You can only delete jobs you created");
        }
        
        jobRepo.delete(job);
        return true;
    }
    
    /**
     * Get job with application count for deletion confirmation
     */
    public JobPostDeleteInfo getJobDeleteInfo(Long id, String recruiterUsername) {
        RegisterRecuiter recruiter = recruiterRepo.findByUsername(recruiterUsername);
        if (recruiter == null) {
            throw new IllegalArgumentException("Recruiter not found: " + recruiterUsername);
        }
        
        JobPost job = jobRepo.findByIdAndRecruiter(id, recruiter).orElse(null);
        if (job == null) {
            throw new SecurityException("Access denied: You can only delete jobs you created");
        }
        
        // Count applications for this job
        long applicationCount = job.getApplicationCount();
        
        return new JobPostDeleteInfo(job.getId(), job.getTitle(), applicationCount);
    }
    
    /**
     * Inner class for deletion information
     */
    public static class JobPostDeleteInfo {
        private final Long jobId;
        private final String jobTitle;
        private final long applicationCount;
        
        public JobPostDeleteInfo(Long jobId, String jobTitle, long applicationCount) {
            this.jobId = jobId;
            this.jobTitle = jobTitle;
            this.applicationCount = applicationCount;
        }
        
        public Long getJobId() { return jobId; }
        public String getJobTitle() { return jobTitle; }
        public long getApplicationCount() { return applicationCount; }
        public boolean hasApplications() { return applicationCount > 0; }
    }

    // Legacy methods (kept for backward compatibility, but deprecated for recruiter use)
    @Deprecated
    public JobPost save(JobPost job) {
        return jobRepo.save(job);
    }

    @Deprecated
    public void delete(Long id) {
        jobRepo.deleteById(id);
    }
}
