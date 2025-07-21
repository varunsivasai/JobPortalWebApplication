package com.example.HomePage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSeekerProfileService {
    @Autowired
    private JobSeekerProfileRepository repo;
    
    @Autowired
    private RegisterJobSeekerRepository userRepo;

    public JobSeekerProfile save(JobSeekerProfile profile) {
        return repo.save(profile);
    }

    public JobSeekerProfile getByUser(RegisterJobSeeker user) {
        return repo.findByUser(user);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public List<JobSeekerProfile> getAllProfiles() {
        return repo.findAll();  // Return all profiles
    }

    public RegisterJobSeeker getByUserName(String username) {
        return userRepo.findByUsername(username);
    }
}
