package com.scm.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.entities.User;
import com.scm.entities.UserRole;
import com.scm.helpers.AppConstants;
import com.scm.helpers.Helper;
import com.scm.helpers.ResourceNotFoundException;
import com.scm.repositories.UserRepo;
import com.scm.services.EmailService;
import com.scm.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Helper helper;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder,
            EmailService emailService, Helper helper) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.helper = helper;
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        String userId = UUID.randomUUID().toString();
        user.setUserId(userId);
        // password encode
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // set the user role
        UserRole userRole = UserRole.builder()
                .role(AppConstants.ROLE_USER)
                .user(user)
                .build();
        user.setRoles(List.of(userRole));

        logger.info("Provider: {}", user.getProvider());
        String emailToken = UUID.randomUUID().toString();
        user.setEmailToken(emailToken);
        User savedUser = userRepo.save(user);

        // Send verification email
        String emailLink = helper.getLinkForEmailVerificatiton(emailToken);

        String htmlEmail = buildVerificationEmail(savedUser.getName(), emailLink);

        logger.info("Sending verification email to: {}", savedUser.getEmail());
        logger.info("Verification link: {}", emailLink);

        try {
            emailService.sendHtmlEmail(
                    savedUser.getEmail(),
                    "Verify Your Account - Smart Contact Manager",
                    htmlEmail);
            logger.info("Verification email sent successfully to: {}", savedUser.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", savedUser.getEmail(), e.getMessage(), e);
            // Don't throw exception - user is already saved
        }
        return savedUser;
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepo.findById(id);
    }

    @Override
    @Transactional
    public Optional<User> updateUser(User user) {
        User user2 = userRepo.findById(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user2.setName(user.getName());
        user2.setEmail(user.getEmail());
        user2.setPassword(user.getPassword());
        user2.setAbout(user.getAbout());
        user2.setPhoneNumber(user.getPhoneNumber());
        user2.setProfilePic(user.getProfilePic());
        user2.setEnabled(user.isEnabled());
        user2.setEmailVerified(user.isEmailVerified());
        user2.setPhoneVerified(user.isPhoneVerified());
        user2.setProvider(user.getProvider());
        user2.setProviderUserId(user.getProviderUserId());
        User save = userRepo.save(user2);
        return Optional.ofNullable(save);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user2 = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepo.delete(user2);
    }

    @Override
    public boolean isUserExist(String userId) {
        // Use existsById instead of findById().isPresent() to avoid loading the full entity
        return userRepo.existsById(userId);
    }

    @Override
    public boolean isUserExistByEmail(String email) {
        // Use existsByEmail for efficient existence check (COUNT query instead of full SELECT)
        return userRepo.existsByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    /**
     * Builds a professional HTML verification email with a clickable button.
     */
    private String buildVerificationEmail(String userName, String verificationLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0; padding:0; background-color:#f3f4f6; font-family:'Segoe UI',Arial,sans-serif;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f3f4f6; padding:40px 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background:linear-gradient(135deg,#4f46e5,#7c3aed); padding:32px 40px; text-align:center;">
                                            <h1 style="margin:0; color:#ffffff; font-size:28px; font-weight:700;">
                                                📱 Smart Contact Manager
                                            </h1>
                                        </td>
                                    </tr>
                                    <!-- Body -->
                                    <tr>
                                        <td style="padding:40px;">
                                            <h2 style="margin:0 0 16px; color:#1f2937; font-size:22px;">
                                                Welcome, %s! 👋
                                            </h2>
                                            <p style="margin:0 0 24px; color:#4b5563; font-size:16px; line-height:1.6;">
                                                Thank you for registering with Smart Contact Manager!
                                                Please verify your email address by clicking the button below.
                                            </p>
                                            <!-- CTA Button -->
                                            <table width="100%%" cellpadding="0" cellspacing="0">
                                                <tr>
                                                    <td align="center" style="padding:8px 0 32px;">
                                                        <a href="%s"
                                                           style="display:inline-block; background:linear-gradient(135deg,#4f46e5,#7c3aed);
                                                                  color:#ffffff; text-decoration:none; padding:16px 48px;
                                                                  border-radius:12px; font-size:18px; font-weight:600;
                                                                  box-shadow:0 4px 12px rgba(79,70,229,0.4);">
                                                            ✅ Verify My Email
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            <p style="margin:0 0 8px; color:#6b7280; font-size:14px;">
                                                Or copy and paste this link in your browser:
                                            </p>
                                            <p style="margin:0 0 24px; word-break:break-all;">
                                                <a href="%s" style="color:#4f46e5; font-size:13px;">%s</a>
                                            </p>
                                            <hr style="border:none; border-top:1px solid #e5e7eb; margin:24px 0;">
                                            <p style="margin:0; color:#9ca3af; font-size:13px;">
                                                If you did not create this account, please ignore this email.
                                                This link will expire after first use.
                                            </p>
                                        </td>
                                    </tr>
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color:#f9fafb; padding:24px 40px; text-align:center; border-top:1px solid #e5e7eb;">
                                            <p style="margin:0; color:#9ca3af; font-size:13px;">
                                                &copy; 2025 Smart Contact Manager &bull; All rights reserved
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(userName, verificationLink, verificationLink, verificationLink);
    }
}
