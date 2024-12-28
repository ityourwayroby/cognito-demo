package com.sgl.sglscholar.controller;

import com.sgl.sglscholar.service.CognitoService;
import com.sgl.sglscholar.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfilePictureController {

    private final S3Service s3Service;
    private final CognitoService cognitoService;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    public ProfilePictureController(S3Service s3Service, CognitoService cognitoService) {
        this.s3Service = s3Service;
        this.cognitoService = cognitoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "File is required"));
        }

        try {
            // Extract the ID token from the security context
            String idToken = extractIdToken();
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "ID Token is missing"));
            }

            // Create a temporary file to upload
            File tempFile = Files.createTempFile("profile_", file.getOriginalFilename()).toFile();
            file.transferTo(tempFile);

            // Use CognitoService to get temporary AWS credentials
            AwsSessionCredentials awsSessionCredentials = cognitoService.getAWSCredentials(idToken);

            // Upload file to S3
            s3Service.uploadFileToS3(awsSessionCredentials, bucketName, tempFile);

            // Generate a presigned URL for the uploaded file
            String fileUrl = s3Service.generatePresignedUrl(awsSessionCredentials, bucketName, tempFile.getName());

            // Delete the temporary file
            tempFile.delete();

            // Send JSON response with the file URL
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error uploading file: " + e.getMessage()));
        }
    }

    private String extractIdToken() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) principal;
            return oidcUser.getIdToken().getTokenValue();
        }
        return null;
    }
}
