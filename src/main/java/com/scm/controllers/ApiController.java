package com.scm.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.scm.entities.Contact;
import com.scm.repositories.GroupMemberRepo;
import com.scm.services.ContactService;
import com.scm.services.EmailService;
import com.scm.services.ImageService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final ContactService contactService;
    private final EmailService emailService;
    private final ImageService imageService;
    private final GroupMemberRepo groupMemberRepo;

    public ApiController(ContactService contactService, EmailService emailService,
            ImageService imageService, GroupMemberRepo groupMemberRepo) {
        this.contactService = contactService;
        this.emailService = emailService;
        this.imageService = imageService;
        this.groupMemberRepo = groupMemberRepo;
    }

    @GetMapping("/contacts/{contactId}")
    public Contact getContact(@PathVariable String contactId) {
        return contactService.getById(contactId);
    }

    // Changed from GET to PUT — toggling favorite mutates data
    @PutMapping("/contacts/{contactId}/toggle-favorite")
    public Contact toggleFavorite(@PathVariable String contactId) {
        Contact contact = contactService.getById(contactId);
        contact.setFavorite(!contact.isFavorite());
        return contactService.update(contact);
    }

    // Changed from GET to DELETE — deleting a resource should use DELETE method
    @DeleteMapping("/contacts/{contactId}")
    @Transactional
    public ResponseEntity<String> deleteContact(@PathVariable String contactId) {
        try {
            logger.info("Attempting to delete contact: {}", contactId);
            Contact contact = contactService.getById(contactId);
            logger.info("Contact found: {}", contact.getName());

            // Use repository query instead of loading all members and filtering in Java (N+1 fix)
            var groupMembers = groupMemberRepo.findByContact_Id(contactId);
            groupMemberRepo.deleteAll(groupMembers);
            logger.info("Deleted {} group memberships", groupMembers.size());

            // Try to delete image, but don't fail if it errors
            if (contact.getCloudinaryImagePublicId() != null && !contact.getCloudinaryImagePublicId().isEmpty()) {
                try {
                    imageService.deleteImage(contact.getCloudinaryImagePublicId());
                    logger.info("Deleted image from Cloudinary: {}", contact.getCloudinaryImagePublicId());
                } catch (Exception e) {
                    logger.warn("Failed to delete image from Cloudinary: {}", e.getMessage());
                }
            }

            contactService.delete(contactId);
            logger.info("Contact deleted successfully: {}", contactId);
            return ResponseEntity.ok("Contact deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting contact: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to delete contact: " + e.getMessage());
        }
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment) {
        try {
            logger.info("Attempting to send email to: {}", to);
            logger.info("Subject: {}", subject);
            logger.info("Has attachment: {}", attachment != null && !attachment.isEmpty());

            if (attachment != null && !attachment.isEmpty()) {
                emailService.sendEmailWithAttachment(to, subject, message, attachment);
            } else {
                emailService.sendEmail(to, subject, message);
            }
            logger.info("Email sent successfully to: {}", to);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send email: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }
}
