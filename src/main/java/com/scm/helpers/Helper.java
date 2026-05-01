package com.scm.helpers;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class Helper {

    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    @Value("${server.baseUrl}")
    private String baseUrl;

    public static String getEmailOfLoggedInUser(Authentication authentication) {

        if (authentication instanceof OAuth2AuthenticationToken) {

            var aOAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            var clientId = aOAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

            var oauth2User = (OAuth2User) authentication.getPrincipal();
            String username = "";

            if (clientId.equalsIgnoreCase("google")) {
                logger.debug("Getting email from google");
                username = Objects.toString(oauth2User.getAttribute("email"), "");

            } else if (clientId.equalsIgnoreCase("github")) {
                logger.debug("Getting email from github");
                String email = Objects.toString(oauth2User.getAttribute("email"), null);
                String login = Objects.toString(oauth2User.getAttribute("login"), "unknown");
                username = email != null ? email : login + "@gmail.com";
            }

            return username;

        } else {
            logger.debug("Getting data from local database");
            return authentication.getName();
        }
    }

    public String getLinkForEmailVerificatiton(String emailToken) {
        String link = this.baseUrl + "/auth/verify-email?token=" + emailToken;
        logger.info("Generated verification link: {}", link);
        return link;
    }
}
