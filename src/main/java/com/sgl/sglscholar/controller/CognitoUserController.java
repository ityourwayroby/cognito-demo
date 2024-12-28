package com.sgl.sglscholar.controller;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/user")
public class CognitoUserController {

    /**
     * Fetch the ID Token granted by Cognito after login.
     */
    @GetMapping("/id-token")
    public String getIdToken(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return "User is not authenticated.";
        }

        // Extract the ID Token from OIDC user
        String idToken = oidcUser.getIdToken().getTokenValue();

        return idToken; // Return only the ID Token
    }
}
