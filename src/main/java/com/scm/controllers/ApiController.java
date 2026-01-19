package com.scm.controllers;

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
import com.scm.services.ContactService;
import com.scm.services.EmailService;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private EmailService emailService;

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
