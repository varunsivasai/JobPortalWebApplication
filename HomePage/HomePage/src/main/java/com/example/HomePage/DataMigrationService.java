package com.example.HomePage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DataMigrationService {

    @Autowired
    private JobPostRepository jobRepo;
    
    @Autowired
    private RegisterJobRecuiterRepository recruiterRepo;

    @Bean
    ApplicationRunner initializeDatabase() {
        return args -> {
            migrateOrphanedJobPosts();
        };
    }

    @Transactional
    public void migrateOrphanedJobPosts() {
        try {
            // Find all job posts without an assigned recruiter
            List<JobPost> orphanedJobs = jobRepo.findAll().stream()
                .filter(job -> job.getRecruiter() == null)
                .toList();

            if (!orphanedJobs.isEmpty()) {
                System.out.println("Found " + orphanedJobs.size() + " orphaned job posts. Migrating...");
                
                // Get the first recruiter as a fallback owner, or create a default one
                RegisterRecuiter defaultRecruiter = getOrCreateDefaultRecruiter();
                
                // Assign orphaned jobs to the default recruiter
                for (JobPost job : orphanedJobs) {
                    job.setRecruiter(defaultRecruiter);
                    jobRepo.save(job);
                }
                
                System.out.println("Migration completed. All job posts now have assigned recruiters.");
            }
        } catch (Exception e) {
            System.err.println("Error during job post migration: " + e.getMessage());
            // Don't throw exception to avoid application startup failure
        }
    }

    private RegisterRecuiter getOrCreateDefaultRecruiter() {
        // Try to get an existing recruiter
        List<RegisterRecuiter> recruiters = recruiterRepo.findAll();
        
        if (!recruiters.isEmpty()) {
            return recruiters.get(0); // Use first recruiter as default
        }
        
        // Create a default system recruiter if none exists
        RegisterRecuiter defaultRecruiter = new RegisterRecuiter();
        defaultRecruiter.setUsername("system_admin");
        defaultRecruiter.setPassword("temp_password_change_me");
        
        try {
            return recruiterRepo.save(defaultRecruiter);
        } catch (Exception e) {
            // If username already exists, find and return it
            return recruiterRepo.findByUsername("system_admin");
        }
    }
}