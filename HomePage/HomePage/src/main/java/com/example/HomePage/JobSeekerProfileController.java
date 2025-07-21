package com.example.HomePage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.*;

@Controller
@RequestMapping("/profile")
public class JobSeekerProfileController {

    @Autowired
    private JobSeekerProfileService profileService;

    @Autowired
    private RegisterJobSeekerRepository userRepo;

    private final Path uploadDir;

    public JobSeekerProfileController(@Value("${file.upload-dir}") String uploadDirPath) throws IOException {
        this.uploadDir = Paths.get(uploadDirPath);
        if (!Files.exists(this.uploadDir)) {
            Files.createDirectories(this.uploadDir);
        }
    }

    /** View Profile */
    @GetMapping
    public String viewProfile(HttpSession session, Model model,
                              @RequestParam(value = "message", required = false) String message) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        RegisterJobSeeker user = userRepo.findByUsername(username);
        JobSeekerProfile profile = profileService.getByUser(user);

        model.addAttribute("profile", profile);
        model.addAttribute("message", message);
        return "jobseeker_profile";
    }

    /** Show create profile form */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        RegisterJobSeeker user = userRepo.findByUsername(username);
        JobSeekerProfile existingProfile = profileService.getByUser(user);

        if (existingProfile != null) {
            return "redirect:/profile?message=Profile already exists";
        }

        model.addAttribute("profile", new JobSeekerProfile());
        return "create_profile";
    }

    /** Save new profile */
    @PostMapping("/save")
    public String saveProfile(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String jobPosition,
                              @RequestParam("resume") MultipartFile resumeFile,
                              HttpSession session) throws IOException {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        RegisterJobSeeker user = userRepo.findByUsername(username);
        if (profileService.getByUser(user) != null) {
            return "redirect:/profile?message=Profile already exists";
        }

        String fileName = StringUtils.cleanPath(resumeFile.getOriginalFilename());
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(resumeFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setName(name);
        profile.setEmail(email);
        profile.setJobPosition(jobPosition);
        profile.setResumePath(fileName); // Store only filename
        profile.setUser(user);

        profileService.save(profile);
        
        // Check if user was trying to apply for a job and redirect back
        String redirectAfterProfile = (String) session.getAttribute("redirectAfterProfile");
        if (redirectAfterProfile != null) {
            session.removeAttribute("redirectAfterProfile");
            return "redirect:" + redirectAfterProfile;
        }
        
        return "redirect:/profile?message=Profile created successfully";
    }

    /** Edit profile */
    @GetMapping("/edit")
    public String editForm(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        RegisterJobSeeker user = userRepo.findByUsername(username);
        JobSeekerProfile profile = profileService.getByUser(user);

        if (profile == null) {
            return "redirect:/profile/create?message=Please create a profile first";
        }

        model.addAttribute("profile", profile);
        return "edit_profile";
    }

    /** Update profile */
    @PostMapping("/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String jobPosition,
                                @RequestParam(value = "resume", required = false) MultipartFile resumeFile,
                                HttpSession session) throws IOException {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        RegisterJobSeeker user = userRepo.findByUsername(username);
        JobSeekerProfile profile = profileService.getByUser(user);

        if (profile == null) {
            return "redirect:/profile/create?message=Please create a profile first";
        }

        profile.setName(name);
        profile.setEmail(email);
        profile.setJobPosition(jobPosition);

        if (resumeFile != null && !resumeFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(resumeFile.getOriginalFilename());
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(resumeFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            profile.setResumePath(fileName);
        }

        profileService.save(profile);
        return "redirect:/profile?message=Profile updated successfully";
    }

    /** Delete profile */
    @GetMapping("/delete")
    public String deleteProfile(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        RegisterJobSeeker user = userRepo.findByUsername(username);
        JobSeekerProfile profile = profileService.getByUser(user);

        if (profile != null) {
            try {
                Files.deleteIfExists(uploadDir.resolve(profile.getResumePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            profileService.delete(profile.getId());
        }

        return "redirect:/profile?message=Profile deleted";
    }

    /** Download resume - supports both job seeker (no param) and recruiter (with username param) */
    @GetMapping("/download")
    public ResponseEntity<?> downloadResume(@RequestParam(required = false) String username,
                                            HttpSession session) throws IOException {
        // If no username provided, use session username (job seeker's own resume)
        if (username == null) {
            username = (String) session.getAttribute("username");
        }
        
        if (username == null) {
            return ResponseEntity.badRequest().body("Please log in or provide username");
        }

        RegisterJobSeeker user = userRepo.findByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        JobSeekerProfile profile = profileService.getByUser(user);
        if (profile == null || profile.getResumePath() == null) {
            return ResponseEntity.badRequest().body("No resume found for this user");
        }

        Path path = uploadDir.resolve(profile.getResumePath());
        if (!Files.exists(path)) {
            return ResponseEntity.badRequest().body("Resume file not found");
        }

        Resource resource = new UrlResource(path.toUri());
        String fileName = path.getFileName().toString();
        String mimeType = Files.probeContentType(path);
        if (mimeType == null) mimeType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
