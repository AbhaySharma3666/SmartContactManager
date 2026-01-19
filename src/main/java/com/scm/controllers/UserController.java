package com.scm.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.scm.entities.User;
import com.scm.forms.FeedbackForm;
import com.scm.forms.UserUpdateForm;
import com.scm.helpers.Helper;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.services.ImageService;
import com.scm.services.SmsService;
import com.scm.services.UserService;
import com.scm.entities.Feedback;
import com.scm.repositories.FeedbackRepo;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/user")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private FeedbackRepo feedbackRepo;

    private Map<String, String> otpStore = new HashMap<>();

    // user dashbaord page

    @RequestMapping(value = "/dashboard")
    public String userDashboard() {
        System.out.println("User dashboard");
        return "user/dashboard";
    }

    // user profile page

    @RequestMapping(value = "/profile")
    public String userProfile(Model model, Authentication authentication) {
        return "user/profile";
    }

    // update profile
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UserUpdateForm form, HttpSession session, Authentication authentication) {
        try {
            logger.info("Starting profile update");
            String username = Helper.getEmailOfLoggedInUser(authentication);
            User user = userService.getUserByEmail(username);
            logger.info("Current user profile pic: {}", user.getProfilePic());

            user.setName(form.getName());
            user.setAbout(form.getAbout());

            // Handle phone number change
            if (form.getPhoneNumber() != null && !form.getPhoneNumber().equals(user.getPhoneNumber())) {
                user.setPhoneNumber(form.getPhoneNumber());
                user.setPhoneVerified(false);
            }

            // Handle profile image
            if (form.getProfileImage() != null && !form.getProfileImage().isEmpty()) {
                logger.info("Uploading image: {} (size: {} bytes)", 
                    form.getProfileImage().getOriginalFilename(), 
                    form.getProfileImage().getSize());
                try {
                    // Create unique filename with timestamp
                    String originalFilename = form.getProfileImage().getOriginalFilename();
                    String uniqueFilename = "profile_" + user.getUserId() + "_" + System.currentTimeMillis();
                    
                    String imageUrl = imageService.uploadImage(form.getProfileImage(), uniqueFilename);
                    logger.info("Uploaded image URL: {}", imageUrl);
                    user.setProfilePic(imageUrl);
                    logger.info("Set profile pic to: {}", user.getProfilePic());
                } catch (Exception e) {
                    logger.error("Error uploading image", e);
                    throw new RuntimeException("Failed to upload image: " + e.getMessage());
                }
            }

            Optional<User> updatedUserOpt = userService.updateUser(user);
            if (updatedUserOpt.isPresent()) {
                User updatedUser = updatedUserOpt.get();
                logger.info("Profile updated successfully. New profile pic: {}", updatedUser.getProfilePic());
            }
            
            session.setAttribute("message", Message.builder()
                    .content("Profile updated successfully")
                    .type(MessageType.green)
                    .build());
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            session.setAttribute("message", Message.builder()
                    .content("Error updating profile: " + e.getMessage())
                    .type(MessageType.red)
                    .build());
        }
        return "redirect:/user/profile";
    }

    // Send OTP
    @PostMapping("/profile/send-otp")
    @ResponseBody
    public Map<String, Object> sendOTP(@RequestParam String phoneNumber, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            String otp = String.format("%06d", new Random().nextInt(999999));
            otpStore.put(phoneNumber, otp);
            logger.info("Generated OTP for {}: {}", phoneNumber, otp);
            
            String message = "Your OTP for Smart Contact Manager is: " + otp + " Valid for 5 minutes.";
            boolean smsSent = smsService.sendSms(phoneNumber, message);
            
            if (smsSent) {
                logger.info("OTP sent successfully to {}", phoneNumber);
                response.put("success", true);
                response.put("message", "OTP sent successfully to " + phoneNumber);
            } else {
                logger.warn("SMS service not configured. Showing OTP in response for testing.");
                response.put("success", true);
                response.put("message", "SMS service not configured. Your OTP is: " + otp);
                response.put("otp", otp); // For testing without SMS
            }
        } catch (Exception e) {
            logger.error("Error sending OTP", e);
            response.put("success", false);
            response.put("message", "Failed to send OTP: " + e.getMessage());
        }
        return response;
    }

    // Verify OTP
    @PostMapping("/profile/verify-otp")
    @ResponseBody
    public Map<String, Object> verifyOTP(@RequestParam String phoneNumber, @RequestParam String otp, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            String storedOtp = otpStore.get(phoneNumber);
            if (storedOtp != null && storedOtp.equals(otp)) {
                String username = Helper.getEmailOfLoggedInUser(authentication);
                User user = userService.getUserByEmail(username);
                user.setPhoneVerified(true);
                userService.updateUser(user);
                otpStore.remove(phoneNumber);
                response.put("success", true);
                response.put("message", "Phone verified successfully");
            } else {
                response.put("success", false);
                response.put("message", "Invalid OTP");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Verification failed");
        }
        return response;
    }

    // user add contacts page

    // user view contacts

    // user edit contact

    // user delete contact

    // Feedback page
    @GetMapping("/feedback")
    public String feedbackPage(Model model) {
        model.addAttribute("feedbackForm", new FeedbackForm());
        return "user/feedback";
    }

    // Submit feedback
    @PostMapping("/feedback")
    public String submitFeedback(@ModelAttribute FeedbackForm form, HttpSession session, Authentication authentication) {
        try {
            String username = Helper.getEmailOfLoggedInUser(authentication);
            User user = userService.getUserByEmail(username);

            Feedback feedback = new Feedback();
            feedback.setFeedbackId(java.util.UUID.randomUUID().toString());
            feedback.setSubject(form.getSubject());
            feedback.setMessage(form.getMessage());
            feedback.setRating(form.getRating());
            feedback.setCreatedAt(java.time.LocalDateTime.now());
            feedback.setUser(user);

            feedbackRepo.save(feedback);

            session.setAttribute("message", Message.builder()
                    .content("Thank you for your feedback!")
                    .type(MessageType.green)
                    .build());
        } catch (Exception e) {
            logger.error("Error submitting feedback", e);
            session.setAttribute("message", Message.builder()
                    .content("Failed to submit feedback")
                    .type(MessageType.red)
                    .build());
        }
        return "redirect:/user/feedback";
    }

}
