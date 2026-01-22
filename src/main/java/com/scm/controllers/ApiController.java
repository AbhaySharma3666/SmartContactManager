package com.scm.controllers;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    private Logger logger = org.slf4j.LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ContactService contactService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private GroupMemberRepo groupMemberRepo;

    @GetMapping("/contacts/{contactId}")
    public Contact getContact(@PathVariable String contactId) {
        return contactService.getById(contactId);
    }

    @GetMapping("/contacts/{contactId}/toggle-favorite")
    public Contact toggleFavorite(@PathVariable String contactId) {
        Contact contact = contactService.getById(contactId);
        contact.setFavorite(!contact.isFavorite());
        return contactService.update(contact);
    }

    @GetMapping("/contacts/{contactId}/delete")
    @Transactional
    public ResponseEntity<String> deleteContact(@PathVariable String contactId) {
        try {
            logger.info("Attempting to delete contact: {}", contactId);
            Contact contact = contactService.getById(contactId);
            logger.info("Contact found: {}", contact.getName());
            
            // Delete all group memberships first
            var groupMembers = groupMemberRepo.findAll().stream()
                .filter(gm -> gm.getContact() != null && gm.getContact().getId().equals(contactId))
                .toList();
            groupMemberRepo.deleteAll(groupMembers);
            logger.info("Deleted {} group memberships", groupMembers.size());
            
            // Try to delete image, but don't fail if it errors
            if (contact.getCloudinaryImagePublicId() != null && !contact.getCloudinaryImagePublicId().isEmpty()) {
                try {
                    imageService.deleteImage(contact.getCloudinaryImagePublicId());
                    logger.info("Deleted image from Cloudinary: {}", contact.getCloudinaryImagePublicId());
                } catch (Exception e) {
                    logger.warn("Failed to delete image from Cloudinary, continuing with contact deletion: {}", e.getMessage());
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
            if (attachment != null && !attachment.isEmpty()) {
                emailService.sendEmailWithAttachment(to, subject, message, attachment);
            } else {
                emailService.sendEmail(to, subject, message);
            }
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }

}
