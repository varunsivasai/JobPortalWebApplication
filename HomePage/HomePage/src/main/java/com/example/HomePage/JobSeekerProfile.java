package com.example.HomePage;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data  // Lombok: automatically creates getters, setters, toString, equals, hashCode
public class JobSeekerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String jobPosition;
    private String resumePath; // File storage path

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private RegisterJobSeeker user; // Link to login user
}
