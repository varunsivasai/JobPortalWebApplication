package com.example.HomePage;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavigationController {

    /** Job Seeker Dashboard */
    @GetMapping("/jobseeker/homepage")
    public String jobSeekerDashboard(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        return "jobseeker_home";  // Thymeleaf template
    }

    /** Recruiter Dashboard */
    @GetMapping("/recruiter/dashboard")
    public String recruiterDashboard(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        return "recruiter_dashboard";
    }

    /** Logout - clear session and redirect to homepage */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // Clear session
        return "redirect:/";   // Redirect to landing page
    }
}
