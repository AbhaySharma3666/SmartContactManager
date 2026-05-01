package com.scm.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.entities.User;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.repositories.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepo userRepo;

    public AuthController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token,
            HttpServletRequest request, HttpSession session) {

        User user = userRepo.findByEmailToken(token).orElse(null);

        if (user == null) {
            logger.warn("Email verification failed: no user found for token");
            session.setAttribute("message", Message.builder()
                    .type(MessageType.red)
                    .content("Email not verified! Token is invalid or expired.")
                    .build());
            return "error_page";
        }

        // Enable and verify the user
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setEmailToken(null); // Invalidate token to prevent reuse
        userRepo.save(user);
        logger.info("Email verified for user: {}", user.getEmail());

        // Auto-login the user after successful verification
        try {
            UsernamePasswordAuthenticationToken authToken =
                    UsernamePasswordAuthenticationToken.authenticated(
                            user, null, user.getAuthorities());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

            // Persist the security context to the HTTP session
            request.getSession(true).setAttribute(
                    "SPRING_SECURITY_CONTEXT", context);

            logger.info("Auto-login successful for: {}", user.getEmail());

            session.setAttribute("message", Message.builder()
                    .type(MessageType.green)
                    .content("Email verified successfully! Welcome to Smart Contact Manager, "
                            + user.getName() + "!")
                    .build());

            return "redirect:/user/profile";

        } catch (Exception e) {
            logger.error("Auto-login failed after email verification: {}", e.getMessage(), e);

            // Verification succeeded but auto-login failed — let user login manually
            session.setAttribute("message", Message.builder()
                    .type(MessageType.green)
                    .content("Email verified successfully! Please login to continue.")
                    .build());
            return "redirect:/login";
        }
    }
}
