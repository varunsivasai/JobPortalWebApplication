package com.example.HomePage;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String location;

    @Column(length = 2000)
    private String description;

    private LocalDate postedDate = LocalDate.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = true) // Initially nullable for migration
    private RegisterRecuiter recruiter;
    
    // Bidirectional relationship with applications (cascade delete)
    @OneToMany(mappedBy = "jobPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobApplication> applications = new ArrayList<>();
    
    // Helper method to get recruiter username
    public String getRecruiterUsername() {
        return recruiter != null ? recruiter.getUsername() : null;
    }
    
    // Helper method to get application count
    public int getApplicationCount() {
        return applications != null ? applications.size() : 0;
    }
}
