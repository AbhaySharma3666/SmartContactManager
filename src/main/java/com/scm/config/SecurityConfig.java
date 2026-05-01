package com.scm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.scm.services.impl.SecurityCustomUserDetailService;

@Configuration
public class SecurityConfig {

    private final SecurityCustomUserDetailService userDetailService;
    private final OAuthAuthenicationSuccessHandler handler;
    private final AuthFailtureHandler authFailtureHandler;

    public SecurityConfig(SecurityCustomUserDetailService userDetailService,
            OAuthAuthenicationSuccessHandler handler,
            AuthFailtureHandler authFailtureHandler) {
        this.userDetailService = userDetailService;
        this.handler = handler;
        this.authFailtureHandler = authFailtureHandler;
    }

    // configuration of authentication provider for spring security
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        // URL authorization: authenticated vs public
        httpSecurity.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers("/user/**").authenticated();
            authorize.requestMatchers("/api/**").authenticated();
            authorize.anyRequest().permitAll();
        });

        // Form login configuration
        httpSecurity.formLogin(formLogin -> {
            formLogin.loginPage("/login");
            formLogin.loginProcessingUrl("/authenticate");
            // Use defaultSuccessUrl (redirect) instead of successForwardUrl (server forward)
            // successForwardUrl does a POST forward which fails on GET-mapped handlers
            formLogin.defaultSuccessUrl("/user/profile", true);
            formLogin.usernameParameter("email");
            formLogin.passwordParameter("password");
            formLogin.failureHandler(authFailtureHandler);
        });

        httpSecurity.csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/**")
            // Also ignore CSRF for logout since we use a GET link
            .ignoringRequestMatchers("/do-logout")
        );

        // OAuth2 configuration
        httpSecurity.oauth2Login(oauth -> {
            oauth.loginPage("/login");
            oauth.successHandler(handler);
        });

        // Logout configuration
        httpSecurity.logout(logoutForm -> {
            logoutForm.logoutUrl("/do-logout");
            logoutForm.logoutSuccessUrl("/login?logout=true");
            logoutForm.invalidateHttpSession(true);
            logoutForm.deleteCookies("JSESSIONID");
        });

        // Prevent back button after logout by setting proper cache headers
        httpSecurity.headers(headers -> headers
            .cacheControl(cache -> {}) // Sets Cache-Control: no-cache, no-store, must-revalidate
        );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
