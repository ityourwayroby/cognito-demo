package com.sgl.sglscholar;

import java.security.Principal;

import com.sgl.sglscholar.service.CognitoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/api/user")
public class SglscholarApplication {

	@Autowired
	private CognitoService cognitoService; // Inject the CognitoService

	public static void main(String[] args) {
		SpringApplication.run(SglscholarApplication.class, args);
	}

	@GetMapping("/username")
	public String getUsername(Authentication authentication, Principal principal) {
		System.out.println("Authenticated user: " + authentication.getName());
		System.out.println("Principal name: " + principal.getName());
		return authentication.getName(); // Return the username
	}

	/**
	 * Endpoint to fetch the Identity ID for the current user.
	 */
	@GetMapping("/fetch-identity-id")
	public String fetchIdentityId(@AuthenticationPrincipal OidcUser oidcUser) {
		if (oidcUser == null) {
			throw new RuntimeException("OidcUser is null. User is not authenticated.");
		}

		String idToken = oidcUser.getIdToken().getTokenValue();
		return cognitoService.fetchIdentityId(idToken);
	}

	/**
	 * Additional debugging endpoint to verify the authentication context.
	 */
	@GetMapping("/debug-auth")
	public String debugAuth(@AuthenticationPrincipal OidcUser oidcUser, Authentication authentication) {
		if (oidcUser == null) {
			return "OidcUser is null. User is not authenticated.";
		}
		return "Authentication Name: " + authentication.getName() +
				", OidcUser Name: " + oidcUser.getFullName() +
				", ID Token: " + oidcUser.getIdToken().getTokenValue();
	}
}
