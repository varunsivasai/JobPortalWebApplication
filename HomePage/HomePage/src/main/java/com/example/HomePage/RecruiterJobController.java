package com.example.HomePage;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/recruiter/jobs")
public class RecruiterJobController {

    @Autowired
    private JobPostService jobService;

    @Autowired
    private JobSeekerProfileService jobSeekerService;  // FIX: Added service to fetch job seekers
    
    @Autowired
    private JobApplicationRepository applicationRepo;  // To view job applications
    
    @Autowired
    private RecruiterAuthorizationService authService;
    
    @Autowired
    private SimpleDeletionService simpleDeletionService;

    /**
     * Centralized security check for recruiter operations
     */
    private String checkRecruiterAccess(HttpSession session) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        
        if (!authService.isRecruiterLoggedIn(username, role)) {
            return "redirect:/login";
        }
        return null; // Access granted
    }

    // List jobs for the logged-in recruiter only
    @GetMapping
    public String listJobs(Model model, HttpSession session, 
                          @RequestParam(required = false) String search) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        // Check for session messages
        String message = (String) session.getAttribute("jobMessage");
        String error = (String) session.getAttribute("jobError");
        
        if (message != null) {
            model.addAttribute("message", message);
            session.removeAttribute("jobMessage");
        }
        if (error != null) {
            model.addAttribute("error", error);
            session.removeAttribute("jobError");
        }
        
        try {
            List<JobPost> jobs;
            if (search != null && !search.trim().isEmpty()) {
                jobs = jobService.searchJobsByRecruiter(username, search);
                model.addAttribute("searchTerm", search);
            } else {
                jobs = jobService.getJobsByRecruiter(username);
            }
            model.addAttribute("jobs", jobs);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading jobs: " + e.getMessage());
        }
        
        return "job_manage";  // Page to view/edit/delete jobs
    }

    // Show job creation form
    @GetMapping("/create")
    public String createJobForm(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("job", new JobPost());
        return "job_create";
    }

    // Save new job (with recruiter ownership)
    @PostMapping("/save")
    public String saveJob(@ModelAttribute JobPost job, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            jobService.saveJobForRecruiter(job, username);
            session.setAttribute("jobMessage", "Job '" + job.getTitle() + "' created successfully!");
        } catch (Exception e) {
            session.setAttribute("jobError", "Error creating job: " + e.getMessage());
        }
        return "redirect:/recruiter/jobs";
    }

    // Show job edit form (only for recruiter's own jobs)
    @GetMapping("/edit/{id}")
    public String editJobForm(@PathVariable Long id, Model model, HttpSession session) {
        // Security check
        String securityCheck = checkRecruiterAccess(session);
        if (securityCheck != null) return securityCheck;
        
        String username = (String) session.getAttribute("username");
        
        // Double-check authorization for this specific job
        if (!authService.canAccessJob(username, id)) {
            session.setAttribute("jobError", "Access denied: You can only edit jobs you created");
            return "redirect:/recruiter/jobs";
        }
        
        try {
            JobPost job = jobService.getJobByIdAndRecruiter(id, username);
            if (job == null) {
                session.setAttribute("jobError", "Job not found or access denied");
                return "redirect:/recruiter/jobs";
            }
            
            model.addAttribute("job", job);
            return "edit_job";  // Use dedicated edit template
        } catch (Exception e) {
            session.setAttribute("jobError", "Error accessing job: " + e.getMessage());
            return "redirect:/recruiter/jobs";
        }
    }

    // Update job (only for recruiter's own jobs)
    @PostMapping("/update/{id}")
    public String updateJob(@PathVariable Long id, @ModelAttribute JobPost job, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            job.setId(id);
            jobService.saveJobForRecruiter(job, username);
            session.setAttribute("jobMessage", "Job '" + job.getTitle() + "' updated successfully!");
        } catch (SecurityException e) {
            session.setAttribute("jobError", "Access denied: " + e.getMessage());
        } catch (Exception e) {
            session.setAttribute("jobError", "Error updating job: " + e.getMessage());
        }
        return "redirect:/recruiter/jobs";
    }

    // Show delete confirmation page
    @GetMapping("/delete/{id}")
    public String confirmDeleteJob(@PathVariable Long id, Model model, HttpSession session) {
        // Security check
        String securityCheck = checkRecruiterAccess(session);
        if (securityCheck != null) return securityCheck;
        
        String username = (String) session.getAttribute("username");
        
        // Double-check authorization for this specific job
        if (!authService.canAccessJob(username, id)) {
            session.setAttribute("jobError", "Access denied: You can only delete jobs you created");
            return "redirect:/recruiter/jobs";
        }
        
        try {
            JobPostService.JobPostDeleteInfo deleteInfo = jobService.getJobDeleteInfo(id, username);
            model.addAttribute("deleteInfo", deleteInfo);
            return "confirm_delete_job"; // Create this template
        } catch (Exception e) {
            session.setAttribute("jobError", "Error accessing job: " + e.getMessage());
            return "redirect:/recruiter/jobs";
        }
    }
    
    // Actually delete the job (POST request for safety)
    @PostMapping("/delete/{id}")
    public String deleteJobConfirmed(@PathVariable Long id, HttpSession session) {
        // Security check
        String securityCheck = checkRecruiterAccess(session);
        if (securityCheck != null) return securityCheck;
        
        String username = (String) session.getAttribute("username");
        
        // Double-check authorization for this specific job
        if (!authService.canAccessJob(username, id)) {
            session.setAttribute("jobError", "Access denied: You can only delete jobs you created");
            return "redirect:/recruiter/jobs";
        }
        
        try {
            JobPost job = jobService.getJobByIdAndRecruiter(id, username);
            String jobTitle = (job != null) ? job.getTitle() : "Job";
            
            // Use the simple deletion service to handle foreign key constraints
            simpleDeletionService.deleteJobWithApplications(id, username);
            session.setAttribute("jobMessage", "Job '" + jobTitle + "' and all related applications have been deleted successfully!");
        } catch (SecurityException e) {
            session.setAttribute("jobError", "Access denied: " + e.getMessage());
        } catch (Exception e) {
            session.setAttribute("jobError", "Error deleting job: " + e.getMessage());
        }
        return "redirect:/recruiter/jobs";
    }

    // View all job seekers (for recruiter)
    @GetMapping("/jobseekers")
    public String viewJobSeekers(Model model) {
        List<JobSeekerProfile> seekers = jobSeekerService.getAllProfiles();
        model.addAttribute("seekers", seekers);
        return "recruiter_jobseekers";  // Show job seekers list
    }
    
    // View job applications for recruiter's own jobs only
    @GetMapping("/applications")
    public String viewJobApplications(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            // Get all jobs posted by this recruiter
            List<JobPost> recruiterJobs = jobService.getJobsByRecruiter(username);
            
            // Get applications for these jobs
            List<JobApplication> applications = applicationRepo.findByJobPostIn(recruiterJobs);
            
            model.addAttribute("applications", applications);
            return "recruiter_jobapplications";  // Show job applications for recruiter's jobs
        } catch (Exception e) {
            model.addAttribute("error", "Error loading applications: " + e.getMessage());
            return "recruiter_jobapplications";
        }
    }
    
    // Handle job application status updates (accept/reject)
    @PostMapping("/applications/{applicationId}/status")
    public String updateApplicationStatus(@PathVariable Long applicationId, 
                                        @RequestParam String status, 
                                        HttpSession session) {
        // Security check
        String securityCheck = checkRecruiterAccess(session);
        if (securityCheck != null) return securityCheck;
        
        String username = (String) session.getAttribute("username");
        
        try {
            JobApplication application = applicationRepo.findById(applicationId).orElse(null);
            if (application == null) {
                session.setAttribute("jobError", "Application not found");
                return "redirect:/recruiter/jobs/applications";
            }
            
            // Verify that the application is for a job posted by this recruiter
            JobPost jobPost = application.getJobPost();
            if (jobPost == null || !authService.canAccessJob(username, jobPost.getId())) {
                session.setAttribute("jobError", "Access denied: You can only manage applications for your own job posts");
                return "redirect:/recruiter/jobs/applications";
            }
            
            // Update application status
            if ("ACCEPTED".equals(status) || "REJECTED".equals(status)) {
                application.setStatus(status);
                applicationRepo.save(application);
                session.setAttribute("jobMessage", "Application status updated to " + status.toLowerCase());
            } else {
                session.setAttribute("jobError", "Invalid status: " + status);
            }
            
        } catch (Exception e) {
            session.setAttribute("jobError", "Error updating application status: " + e.getMessage());
        }
        
        return "redirect:/recruiter/jobs/applications";
    }
}
