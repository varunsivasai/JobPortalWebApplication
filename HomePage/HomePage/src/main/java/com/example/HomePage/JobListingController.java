package com.example.HomePage;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/jobs")
public class JobListingController {

    @Autowired
    private JobPostRepository jobPostRepo;

    // Show all jobs or filter by search term
    @GetMapping("/landing")
    public String listJobs(@RequestParam(value = "search", required = false) String search, 
                          Model model, HttpSession session) {
        if (search != null && !search.isEmpty()) {
            // Search both title and company for better results
            model.addAttribute("jobs", jobPostRepo.findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(search, search));
        } else {
            model.addAttribute("jobs", jobPostRepo.findAll());
        }
        
        // Check for application messages from session and display them
        String message = (String) session.getAttribute("applicationMessage");
        String error = (String) session.getAttribute("applicationError");
        
        if (message != null) {
            model.addAttribute("message", message);
            session.removeAttribute("applicationMessage"); // Clear after displaying
        }
        
        if (error != null) {
            model.addAttribute("error", error);
            session.removeAttribute("applicationError"); // Clear after displaying
        }
        
        return "job_listings";  // Thymeleaf page
    }
}
