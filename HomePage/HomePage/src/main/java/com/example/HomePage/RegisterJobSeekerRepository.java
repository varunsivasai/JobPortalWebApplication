package com.example.HomePage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterJobSeekerRepository extends JpaRepository<RegisterJobSeeker, Long> {

    RegisterJobSeeker findByUsername(String username);

    boolean existsByUsername(String username);  // Correct method signature
}
