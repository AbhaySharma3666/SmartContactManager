package com.scm.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

import com.scm.entities.User;
import com.scm.forms.FeedbackForm;
import com.scm.forms.GroupForm;
import com.scm.forms.UserUpdateForm;
import com.scm.helpers.Helper;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.services.GroupService;
import com.scm.services.ImageService;
import com.scm.services.SmsService;
import com.scm.services.UserService;
import com.scm.services.DashboardService;
import com.scm.entities.Contact;
import com.scm.entities.ContactGroup;
import com.scm.entities.Feedback;
import com.scm.entities.GroupMember;
import com.scm.entities.DashboardStats;
import com.scm.repositories.ContactRepo;
import com.scm.repositories.FeedbackRepo;
import com.scm.repositories.GroupRepo;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private GroupService groupService;
    
    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private DashboardService dashboardService;

    private Map<String, String> otpStore = new HashMap<>();

    // user dashbaord page

    @RequestMapping(value = "/dashboard")
    public String userDashboard(Model model, Authentication authentication) {
        try {
            String username = Helper.getEmailOfLoggedInUser(authentication);
            User user = userService.getUserByEmail(username);
            
            DashboardStats stats = dashboardService.getDashboardStats(user);
            
            model.addAttribute("totalContacts", stats.getTotalContacts());
            model.addAttribute("favoriteContacts", stats.getFavoriteContacts());
            model.addAttribute("totalGroups", stats.getTotalGroups());
            model.addAttribute("recentContacts", stats.getRecentContacts());
            
        } catch (Exception e) {
            logger.error("Error loading dashboard", e);
            model.addAttribute("totalContacts", 0L);
            model.addAttribute("favoriteContacts", 0L);
            model.addAttribute("totalGroups", 0L);
            model.addAttribute("recentContacts", new ArrayList<>());
        }
        return "user/dashboard";
    }

    // user profile page

    @RequestMapping(value = "/profile")
    public String userProfile(Model model, Authentication authentication) {
        return "user/profile";
    }

    // update profile
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UserUpdateForm form, HttpSession session,
            Authentication authentication) {
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
                    // Delete old image if exists
                    String oldProfilePic = user.getProfilePic();
                    if (oldProfilePic != null && !oldProfilePic.isEmpty()
                            && !oldProfilePic.contains("unknow_user.png")) {
                        String oldPublicId = oldProfilePic.substring(oldProfilePic.lastIndexOf("/") + 1,
                                oldProfilePic.lastIndexOf("."));
                        imageService.deleteImage(oldPublicId);
                        logger.info("Deleted old profile image: {}", oldPublicId);
                    }

                    // Create unique filename with timestamp
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
    public Map<String, Object> verifyOTP(@RequestParam String phoneNumber, @RequestParam String otp,
            Authentication authentication) {
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
    public String submitFeedback(@ModelAttribute FeedbackForm form, HttpSession session,
            Authentication authentication) {
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

    // Groups page
    @GetMapping("/groups")
    public String groupsPage(Model model, Authentication authentication) {
        try {
            String username = Helper.getEmailOfLoggedInUser(authentication);
            User user = userService.getUserByEmail(username);
            
            if(user == null){
                logger.error("User not found: {}", username);
                model.addAttribute("groups", new ArrayList<>());
                return "user/groups";
            }
            
            List<ContactGroup> groups = groupRepo.findByUser(user);
            logger.info("Found {} groups for user {}", groups.size(), username);
            
            List<Map<String, Object>> groupsWithCount = new ArrayList<>();
            for(ContactGroup group : groups){
                try {
                    Map<String, Object> groupData = new HashMap<>();
                    groupData.put("groupId", group.getGroupId());
                    groupData.put("name", group.getName());
                    groupData.put("description", group.getDescription() != null ? group.getDescription() : "");
                    groupData.put("createdAt", group.getCreatedAt());
                    groupData.put("memberCount", groupService.getMemberCount(group.getGroupId()));
                    groupsWithCount.add(groupData);
                } catch (Exception e) {
                    logger.error("Error processing group: {}", group.getGroupId(), e);
                }
            }
            
            logger.info("Returning {} groups with data", groupsWithCount.size());
            model.addAttribute("groups", groupsWithCount);
            return "user/groups";
        } catch (Exception e) {
            logger.error("Error loading groups", e);
            model.addAttribute("groups", new ArrayList<>());
            return "user/groups";
        }
    }

    // Create group
    @PostMapping("/groups/create")
    @ResponseBody
    public Map<String, Object> createGroup(@RequestParam String name, @RequestParam(required = false) String description, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = Helper.getEmailOfLoggedInUser(authentication);
            User user = userService.getUserByEmail(username);

            groupService.createGroup(name, description, user);
            logger.info("Group created: {}", name);

            response.put("success", true);
            response.put("message", "Group created successfully");
        } catch (Exception e) {
            logger.error("Error creating group", e);
            response.put("success", false);
            response.put("message", "Failed to create group");
        }
        return response;
    }

    // Delete group
    @PostMapping("/groups/delete/{groupId}")
    @ResponseBody
    public Map<String, Object> deleteGroup(@PathVariable String groupId) {
        Map<String, Object> response = new HashMap<>();
        try {
            groupService.deleteGroup(groupId);
            logger.info("Group deleted: {}", groupId);
            response.put("success", true);
            response.put("message", "Group deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting group", e);
            response.put("success", false);
            response.put("message", "Failed to delete group");
        }
        return response;
    }

    // Group details page
    @GetMapping("/groups/details/{groupId}")
    public String groupDetailsPage(@PathVariable String groupId, Model model, Authentication authentication) {
        try {
            String username = Helper.getEmailOfLoggedInUser(authentication);
            User user = userService.getUserByEmail(username);
            
            ContactGroup group = groupService.getGroupById(groupId).orElseThrow();
            
            // Verify group belongs to user
            if(!group.getUser().getUserId().equals(user.getUserId())){
                return "redirect:/user/groups";
            }
            
            List<GroupMember> members = groupService.getGroupMembers(groupId);
            List<Contact> allContacts = contactRepo.findByUserId(user.getUserId());
            
            model.addAttribute("group", group);
            model.addAttribute("members", members);
            model.addAttribute("memberCount", members.size());
            model.addAttribute("allContacts", allContacts);
            
            return "user/group_details";
        } catch (Exception e) {
            logger.error("Error loading group details", e);
            return "redirect:/user/groups";
        }
    }
    
    // Update group
    @PostMapping("/groups/update/{groupId}")
    @ResponseBody
    public Map<String, Object> updateGroup(@PathVariable String groupId, 
                                          @RequestParam String name, 
                                          @RequestParam(required = false) String description) {
        Map<String, Object> response = new HashMap<>();
        try {
            ContactGroup group = groupService.getGroupById(groupId).orElseThrow();
            group.setName(name);
            group.setDescription(description);
            groupService.updateGroup(group);
            
            response.put("success", true);
            response.put("message", "Group updated successfully");
        } catch (Exception e) {
            logger.error("Error updating group", e);
            response.put("success", false);
            response.put("message", "Failed to update group");
        }
        return response;
    }
    
    // Add member to group
    @PostMapping("/groups/{groupId}/add-member")
    @ResponseBody
    public Map<String, Object> addMemberToGroup(@PathVariable String groupId, 
                                                @RequestParam String contactId) {
        Map<String, Object> response = new HashMap<>();
        try {
            groupService.addMember(groupId, contactId);
            response.put("success", true);
            response.put("message", "Member added successfully");
            response.put("memberCount", groupService.getMemberCount(groupId));
        } catch (Exception e) {
            logger.error("Error adding member", e);
            response.put("success", false);
            response.put("message", "Failed to add member");
        }
        return response;
    }
    
    // Remove member from group
    @PostMapping("/groups/{groupId}/remove-member")
    @ResponseBody
    public Map<String, Object> removeMemberFromGroup(@PathVariable String groupId, 
                                                     @RequestParam String contactId) {
        Map<String, Object> response = new HashMap<>();
        try {
            groupService.removeMember(groupId, contactId);
            response.put("success", true);
            response.put("message", "Member removed successfully");
            response.put("memberCount", groupService.getMemberCount(groupId));
        } catch (Exception e) {
            logger.error("Error removing member", e);
            response.put("success", false);
            response.put("message", "Failed to remove member");
        }
        return response;
    }

}
