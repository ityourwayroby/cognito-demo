package com.sgl.sglscholar.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // Use a cookie-based CSRF token repository
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll() // Allow anonymous access to "/"
                        .requestMatchers("/error").permitAll() // Allow anonymous access to "/error"
                        .requestMatchers("/api/schools/register").permitAll() // Allow anonymous registration
                        .requestMatchers("/api/user/id-token").authenticated() // Secure ID Token endpoint
                        .requestMatchers("/api/users/administrators").hasRole("Administrators")
                        .requestMatchers("/api/users/teachers").hasRole("Teachers")
                        .requestMatchers("/api/users/students").hasRole("Students")
                        .requestMatchers("/api/profile/upload").authenticated()
                        .anyRequest().authenticated() // All other requests require authentication
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler()) // Custom success handler
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(userAuthoritiesMapper())
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // Logout endpoint (POST only)
                        .logoutSuccessHandler((HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication) -> {
                            String clientId = "27k92jhkela082a65ui9jlihol"; // Replace with your Cognito client ID
                            String logoutUri = "http://localhost:8089"; // Replace with your application's logout redirect URI
                            String cognitoLogoutUrl = String.format(
                                    "https://sglscholarapp.auth.us-east-1.amazoncognito.com/logout?client_id=%s&logout_uri=%s",
                                    clientId, logoutUri
                            );
                            response.sendRedirect(cognitoLogoutUrl); // Redirect to Cognito logout URL
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID") // Clear session and cookies
                );

        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            Optional<OidcUserAuthority> oidcUserAuthority = authorities.stream()
                    .filter(grantedAuthority -> grantedAuthority instanceof OidcUserAuthority)
                    .map(grantedAuthority -> (OidcUserAuthority) grantedAuthority)
                    .findFirst();

            if (oidcUserAuthority.isPresent()) {
                Map<String, Object> attributes = oidcUserAuthority.get().getAttributes();
                Collection<String> cognitoGroups = (Collection<String>) attributes.get("cognito:groups");

                if (cognitoGroups != null) {
                    mappedAuthorities.addAll(cognitoGroups.stream()
                            .map(group -> new SimpleGrantedAuthority("ROLE_" + group))
                            .collect(Collectors.toSet()));
                }
            }

            return mappedAuthorities;
        };
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.core.Authentication authentication) -> {
            String redirectUrl = "/";
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            for (GrantedAuthority authority : authorities) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_Administrators")) {
                    redirectUrl = "/api/users/administrators";
                    break;
                } else if (role.equals("ROLE_Teachers")) {
                    redirectUrl = "/api/users/teachers";
                    break;
                } else if (role.equals("ROLE_Students")) {
                    redirectUrl = "/api/users/students";
                    break;
                }
            }
            response.sendRedirect(redirectUrl);
        };
    }
}
