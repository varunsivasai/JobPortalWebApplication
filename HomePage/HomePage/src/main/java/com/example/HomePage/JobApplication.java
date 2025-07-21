package com.example.HomePage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private JobSeekerProfile jobSeeker;

    @ManyToOne
    private JobPost jobPost;

    private String status = "PENDING";  // Default status
}
