package com.example.HomePage;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/applications")
public class JobApplicationController {

    @Autowired
    private JobPostService jobPostService;

    @Autowired
    private JobApplicationRepository applicationRepo;

    @Autowired
    private JobSeekerProfileService profileService;

    // Apply for a job
    @PostMapping("/apply/{jobId}")
    public String applyForJob(@PathVariable Long jobId, HttpSession session) {
        String username = (String) session.getAttribute("username");

        // If not logged in, save redirect URL and go to login
        if (username == null) {
            session.setAttribute("redirectAfterLogin", "/applications/apply/" + jobId);
            return "redirect:/login";
        }

        RegisterJobSeeker user = profileService.getByUserName(username);
        JobSeekerProfile seeker = (user != null) ? profileService.getByUser(user) : null;
        JobPost job = jobPostService.getJobById(jobId);

        // Check if user has a profile first
        if (user == null) {
            session.setAttribute("applicationError", "User not found. Please log in again.");
            return "redirect:/jobs/landing";
        }
        
        if (seeker == null) {
            // No profile exists, redirect to create profile
            session.setAttribute("redirectAfterProfile", "/applications/apply/" + jobId);
            session.setAttribute("applicationError", "Please create your profile first before applying for jobs.");
            return "redirect:/profile/create";
        }
        
        if (job == null) {
            session.setAttribute("applicationError", "Job not found.");
            return "redirect:/jobs/landing";
        }

        // Prevent duplicate applications
        if (!applicationRepo.existsByJobSeeker_IdAndJobPost_Id(seeker.getId(), job.getId())) {
            JobApplication app = new JobApplication();
            app.setJobPost(job);
            app.setJobSeeker(seeker);
            app.setStatus("PENDING");
            applicationRepo.save(app);
            
            // Add success message
            session.setAttribute("applicationMessage", "Your application for '" + job.getTitle() + "' has been submitted successfully!");
        } else {
            // User already applied for this job
            session.setAttribute("applicationError", "You have already applied for this job.");
        }

        return "redirect:/jobs/landing";  // Return to job listings
    }

    // View my applications (for job seeker)
    @GetMapping("/my-applications")
    public String viewMyApplications(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        RegisterJobSeeker user = profileService.getByUserName(username);
        if (user == null) return "redirect:/login";

        JobSeekerProfile seeker = profileService.getByUser(user);
        if (seeker == null) return "redirect:/profile/create";  // Force profile creation if missing

        List<JobApplication> apps = applicationRepo.findByJobSeeker_Id(seeker.getId());
        model.addAttribute("applications", apps);
        return "job_applications";
    }
    
    // Update application status (for recruiters)
    @PostMapping("/{applicationId}/update")
    public String updateApplicationStatus(@PathVariable Long applicationId, 
                                        @RequestParam String status,
                                        HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";
        
        JobApplication app = applicationRepo.findById(applicationId).orElse(null);
        if (app != null && ("PENDING".equals(status) || "ACCEPTED".equals(status) || "REJECTED".equals(status))) {
            app.setStatus(status);
            applicationRepo.save(app);
        }
        
        return "redirect:/recruiter/jobs/applications";
    }
}
