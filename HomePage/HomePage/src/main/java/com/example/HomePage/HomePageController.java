package com.example.HomePage;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomePageController {

    @Autowired
    private RegisterJobSeekerRepository jobSeekerRepository;

    @Autowired
    private RegisterJobRecuiterRepository recruiterRepository;

    @GetMapping("/")
    public String homepage() {
        return "HomePage";
    }

    @GetMapping("/register/jobseeker")
    public String registerJobseekerPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("msg", error);
        return "Register-jobseeker";
    }

    @PostMapping("/register/jobseeker")
    public String registerJobseeker(@RequestParam String username,
                                    @RequestParam String password,
                                    HttpSession session,
                                    Model model) {
        try {
            RegisterJobSeeker existingJs = jobSeekerRepository.findByUsername(username);
            if (existingJs != null) {
                Integer attempts = (Integer) session.getAttribute("jobseekerRegisterAttempts");
                attempts = (attempts == null) ? 1 : attempts + 1;
                session.setAttribute("jobseekerRegisterAttempts", attempts);

                if (attempts >= 2) {
                    model.addAttribute("msg", "Account already exists. If you forgot your password, use the link below.");
                    model.addAttribute("showForgotPassword", true);
                } else {
                    model.addAttribute("msg", "Username already exists. Try again.");
                }
                return "Register-jobseeker";
            }

            session.removeAttribute("jobseekerRegisterAttempts");
            RegisterJobSeeker js = new RegisterJobSeeker();
            js.setUsername(username);
            js.setPassword(password);
            jobSeekerRepository.save(js);
            return "registerJobSeekerSuccessfully";
        } catch (Exception e) {
            model.addAttribute("msg", "Registration failed. Please try again.");
            return "Register-jobseeker";
        }
    }

    @GetMapping("/register/recruiter")
    public String registerRecruiterPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("msg", error);
        return "Register-recuiter";
    }

    @PostMapping("/register/recruiter")
    public String registerRecruiter(@RequestParam String username,
                                    @RequestParam String password,
                                    HttpSession session,
                                    Model model) {
        try {
            RegisterRecuiter existingRec = recruiterRepository.findByUsername(username);
            if (existingRec != null) {
                Integer attempts = (Integer) session.getAttribute("recruiterRegisterAttempts");
                attempts = (attempts == null) ? 1 : attempts + 1;
                session.setAttribute("recruiterRegisterAttempts", attempts);

                if (attempts >= 2) {
                    model.addAttribute("msg", "Account already exists. Tap 'Forgot Password' to recover.");
                    model.addAttribute("showForgotPassword", true);
                } else {
                    model.addAttribute("msg", "Username already exists. Try again.");
                }
                return "Register-recuiter";
            }

            session.removeAttribute("recruiterRegisterAttempts");
            RegisterRecuiter rec = new RegisterRecuiter();
            rec.setUsername(username);
            rec.setPassword(password);
            recruiterRepository.save(rec);
            return "registerRecuiterSuccessfully";
        } catch (Exception e) {
            model.addAttribute("msg", "Registration failed. Please try again.");
            return "Register-recuiter";
        }
    }

    @GetMapping("/login/jobseeker")
    public String loginJobseekerPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("msg", error);
        return "LoginJobseekerPage";
    }

    @GetMapping("/login/recruiter")
    public String loginRecruiterPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("msg", error);
        return "LoginRecuiterPage";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String role,
                        HttpSession session,
                        Model model) {
        try {
            if ("jobseeker".equalsIgnoreCase(role)) {
                RegisterJobSeeker js = jobSeekerRepository.findByUsername(username);
                if (js != null && js.getPassword().equals(password)) {
                    session.setAttribute("username", username);
                    session.setAttribute("role", "jobseeker");
                    return handlePostLoginRedirect(session, "redirect:/jobseeker/homepage");
                }
            } else if ("recruiter".equalsIgnoreCase(role)) {
                RegisterRecuiter rec = recruiterRepository.findByUsername(username);
                if (rec != null && rec.getPassword().equals(password)) {
                    session.setAttribute("username", username);
                    session.setAttribute("role", "recruiter");
                    return handlePostLoginRedirect(session, "redirect:/recruiter/dashboard");
                }
            } else {
                model.addAttribute("msg", "Invalid role selected.");
                return "HomePage";
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
        }

        model.addAttribute("msg", "Login failed. Please check your credentials or use 'Forgot Password'.");
        model.addAttribute("showForgotPassword", true);
        return "jobseeker".equalsIgnoreCase(role) ? "LoginJobseekerPage" : "LoginRecuiterPage";
    }

    private String handlePostLoginRedirect(HttpSession session, String defaultRedirect) {
        String redirectAfterLogin = (String) session.getAttribute("redirectAfterLogin");
        if (redirectAfterLogin != null) {
            session.removeAttribute("redirectAfterLogin");
            return "redirect:" + redirectAfterLogin;
        }
        return defaultRedirect;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "ForgotPasswordPage";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String username, Model model) {
        try {
            boolean found = false;

            RegisterJobSeeker js = jobSeekerRepository.findByUsername(username);
            if (js != null) {
                found = true;
                model.addAttribute("username", username);
                model.addAttribute("message", "Reset link generated (Jobseeker). Click below to set a new password.");
                return "redirect:/reset-password?username=" + username;
            }

            RegisterRecuiter rec = recruiterRepository.findByUsername(username);
            if (rec != null) {
                found = true;
                model.addAttribute("username", username);
                model.addAttribute("message", "Reset link generated (Recruiter). Click below to set a new password.");
                return "redirect:/reset-password?username=" + username;
            }

            if (!found) {
                model.addAttribute("message", "Username not found. Please check and try again.");
            }

        } catch (Exception e) {
            model.addAttribute("message", "Something went wrong. Please try again later.");
        }

        return "ForgotPasswordPage";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "ResetPasswordPage";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String username,
                                       @RequestParam String newPassword,
                                       Model model) {
        boolean updated = false;
        boolean isRecruiter = false;

        RegisterJobSeeker js = jobSeekerRepository.findByUsername(username);
        if (js != null) {
            js.setPassword(newPassword);
            jobSeekerRepository.save(js);
            updated = true;
        }

        RegisterRecuiter rec = recruiterRepository.findByUsername(username);
        if (rec != null) {
            rec.setPassword(newPassword);
            recruiterRepository.save(rec);
            updated = true;
            isRecruiter = true;
        }

        if (updated) {
            model.addAttribute("msg", "Password updated successfully. Please sign in.");
            return isRecruiter ? "LoginRecuiterPage" : "LoginJobseekerPage";
        } else {
            model.addAttribute("msg", "Username not found. Try again.");
            model.addAttribute("username", username);
            return "ResetPasswordPage";
        }
    }
}