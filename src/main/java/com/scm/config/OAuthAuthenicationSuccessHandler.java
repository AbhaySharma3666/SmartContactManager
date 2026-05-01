package com.scm.config;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.scm.entities.Providers;
import com.scm.entities.User;
import com.scm.entities.UserRole;
import com.scm.helpers.AppConstants;
import com.scm.repositories.UserRepo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuthAuthenicationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuthAuthenicationSuccessHandler.class);

    private final UserRepo userRepo;

    public OAuthAuthenicationSuccessHandler(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        logger.info("OAuthAuthenicationSuccessHandler");

        var oauth2AuthenicationToken = (OAuth2AuthenticationToken) authentication;
        String authorizedClientRegistrationId = oauth2AuthenicationToken.getAuthorizedClientRegistrationId();

        logger.info("Provider: {}", authorizedClientRegistrationId);

        var oauthUser = (DefaultOAuth2User) authentication.getPrincipal();

        oauthUser.getAttributes().forEach((key, value) -> {
            logger.info("{} : {}", key, value);
        });

        // Extract user attributes based on provider
        String email = null;
        String name = null;
        String profilePic = null;
        String providerUserId = oauthUser.getName();
        Providers provider = null;
        String about = null;

        if (authorizedClientRegistrationId.equalsIgnoreCase("google")) {
            email = Objects.toString(oauthUser.getAttribute("email"), null);
            profilePic = Objects.toString(oauthUser.getAttribute("picture"), null);
            name = Objects.toString(oauthUser.getAttribute("name"), "Google User");
            provider = Providers.GOOGLE;
            about = "This account is created using google.";

        } else if (authorizedClientRegistrationId.equalsIgnoreCase("github")) {
            String rawEmail = Objects.toString(oauthUser.getAttribute("email"), null);
            String login = Objects.toString(oauthUser.getAttribute("login"), "unknown");
            email = rawEmail != null ? rawEmail : login + "@gmail.com";
            profilePic = Objects.toString(oauthUser.getAttribute("avatar_url"), null);
            name = login;
            provider = Providers.GITHUB;
            about = "This account is created using github";

        } else if (authorizedClientRegistrationId.equalsIgnoreCase("linkedin")) {
            // LinkedIn OAuth - to be implemented
            logger.info("LinkedIn provider detected - not yet implemented");

        } else {
            logger.info("OAuthAuthenicationSuccessHandler: Unknown provider");
        }

        if (email == null) {
            logger.error("Could not extract email from OAuth provider: {}", authorizedClientRegistrationId);
            response.sendRedirect("/login?error=true");
            return;
        }

        // Check if user already exists
        User existingUser = userRepo.findByEmail(email).orElse(null);
        if (existingUser == null) {
            // Create new user
            User user = new User();
            user.setUserId(UUID.randomUUID().toString());
            user.setEmail(email);
            user.setName(name);
            user.setProfilePic(profilePic);
            user.setProviderUserId(providerUserId);
            user.setProvider(provider);
            user.setAbout(about);
            user.setEmailVerified(true);
            user.setEnabled(true);
            user.setPassword("oauth2-no-password");

            UserRole userRole = UserRole.builder()
                    .role(AppConstants.ROLE_USER)
                    .user(user)
                    .build();
            user.setRoles(List.of(userRole));
            userRepo.save(user);
            logger.info("New OAuth user saved: {}", email);
        } else {
            // Update existing user's profile info from OAuth provider
            boolean updated = false;
            if (name != null && !name.equals(existingUser.getName())) {
                existingUser.setName(name);
                updated = true;
            }
            if (profilePic != null && !profilePic.equals(existingUser.getProfilePic())) {
                existingUser.setProfilePic(profilePic);
                updated = true;
            }
            if (updated) {
                userRepo.save(existingUser);
                logger.info("Updated OAuth user info: {}", email);
            }
        }

        new DefaultRedirectStrategy().sendRedirect(request, response, "/user/profile");
    }
}
