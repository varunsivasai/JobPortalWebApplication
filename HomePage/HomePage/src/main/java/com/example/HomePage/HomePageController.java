package com.example.HomePage;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomePageController {

    @Autowired
    private RegisterJobSeekerRepository jobSeekerRepository;

    @Autowired
    private RegisterJobRecuiterRepository recuiterRepository;

    @GetMapping("/")
    public String homepage() {
        return "HomePage";  // Landing page (with login/register links)
    }

    @GetMapping("/register/jobseeker")
    public String registerJobseekerPage() {
        return "Register-jobseeker";
    }

    @PostMapping("/register/jobseeker")
    public String registerJobseeker(@RequestParam String username, @RequestParam String password) {
        try {
            // Check if username already exists
            RegisterJobSeeker existingJs = jobSeekerRepository.findByUsername(username);
            if (existingJs != null) {
                return "redirect:/register/jobseeker?error=Username already exists";
            }
            
            RegisterJobSeeker js = new RegisterJobSeeker();
            js.setUsername(username);
            js.setPassword(password);
            jobSeekerRepository.save(js);
            return "registerJobSeekerSuccesfully";
        } catch (Exception e) {
            System.err.println("Error registering job seeker: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/register/jobseeker?error=Registration failed: " + e.getMessage();
        }
    }

    @GetMapping("/register/recruiter")
    public String registerRecuiterPage() {
        return "Register-recuiter";
    }

    @PostMapping("/register/recruiter")
    public String registerRecuiter(@RequestParam String username, @RequestParam String password) {
        try {
            // Check if username already exists
            RegisterRecuiter existingRec = recuiterRepository.findByUsername(username);
            if (existingRec != null) {
                // Username already exists - redirect with error
                return "redirect:/register/recruiter?error=Username already exists";
            }
            
            RegisterRecuiter rec = new RegisterRecuiter();
            rec.setUsername(username);
            rec.setPassword(password);
            recuiterRepository.save(rec);
            return "registerRecuiterSuccessfully";
        } catch (Exception e) {
            // Log the error and return error page
            System.err.println("Error registering recruiter: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/register/recruiter?error=Registration failed: " + e.getMessage();
        }
    }

    @GetMapping("/login/jobseeker")
    public String loginPage() {
        return "LoginJobseekerPage"; // Generic login page
    }
    @GetMapping("/login/recruiter")
    public String loginRecruiterPage() {
        return "LoginRecuiterPage"; // Recruiter login page
    }

    @GetMapping("/login")
    public String genericLoginPage() {
        return "LoginJobseekerPage"; // Generic login page
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String role,
                        HttpSession session) {

        try {
            // Check Job Seeker login
            if ("jobseeker".equalsIgnoreCase(role)) {
                RegisterJobSeeker js = jobSeekerRepository.findByUsername(username);
                if (js != null && js.getPassword().equals(password)) {
                    session.setAttribute("username", username);
                    session.setAttribute("role", "jobseeker");
                    return handlePostLoginRedirect(session, "redirect:/jobseeker/homepage");
                }
            }

            // Check Recruiter login
            else if ("recruiter".equalsIgnoreCase(role)) {
                RegisterRecuiter rec = recuiterRepository.findByUsername(username);
                if (rec != null && rec.getPassword().equals(password)) {
                    session.setAttribute("username", username);
                    session.setAttribute("role", "recruiter");
                    return handlePostLoginRedirect(session, "redirect:/recruiter/dashboard");
                }
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
        }

        return "loginFailed"; // Show login failed page
    }

    // Redirect logic (handles Apply Job redirect)
    private String handlePostLoginRedirect(HttpSession session, String defaultRedirect) {
        String redirectAfterLogin = (String) session.getAttribute("redirectAfterLogin");
        if (redirectAfterLogin != null) {
            session.removeAttribute("redirectAfterLogin");
            return "redirect:" + redirectAfterLogin;
        }
        return defaultRedirect;
    }
}
