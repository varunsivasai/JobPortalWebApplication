package com.example.HomePage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSeekerProfileRepository extends JpaRepository<JobSeekerProfile, Long> {

    JobSeekerProfile findByUser_Username(String username);

    JobSeekerProfile findByUser(RegisterJobSeeker user);
}
