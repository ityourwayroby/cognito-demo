package com.sgl.sglscholar.service;

import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdResponse;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityResponse;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CognitoService {

    @Value("${aws.cognito.identity.pool.id}")
    private String identityPoolId;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.cognito.identity.logins.key}")
    private String loginsKey;

    // In-memory cache to store credentials
    private final ConcurrentHashMap<String, CachedCredentials> credentialsCache = new ConcurrentHashMap<>();

    /**
     * Fetches the AWS Identity ID for the given ID token.
     */
    public String fetchIdentityId(String idToken) {
        CognitoIdentityClient cognitoIdentityClient = CognitoIdentityClient.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .build();

        String processedLoginsKey = loginsKey.replace("https://", "");
        System.out.println("Logins Key: " + processedLoginsKey);
        System.out.println("Identity Pool ID: " + identityPoolId);

        GetIdRequest getIdRequest = GetIdRequest.builder()
                .identityPoolId(identityPoolId)
                .logins(Map.of(processedLoginsKey, idToken))
                .build();

        try {
            GetIdResponse getIdResponse = cognitoIdentityClient.getId(getIdRequest);
            String identityId = getIdResponse.identityId();
            System.out.println("Fetched Identity ID: " + identityId);
            return identityId;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch IdentityId", e);
        }
    }

    /**
     * Retrieves temporary AWS credentials for the given ID token.
     */
    public AwsSessionCredentials getAWSCredentials(String idToken) {
        String identityId = fetchIdentityId(idToken);

        return credentialsCache.compute(identityId, (key, cached) -> {
            // Use valid cached credentials if available
            if (cached != null && cached.isValid()) {
                System.out.println("Cached credentials are valid for Identity ID: " + key);
                return cached;
            }

            // Fetch new credentials and update cache
            System.out.println("Fetching new credentials for Identity ID: " + key);
            AwsSessionCredentials credentials = fetchNewCredentials(identityId, idToken);
            Instant expiration = fetchCredentialsExpiration(credentials);
            return new CachedCredentials(credentials, expiration);
        }).getAwsSessionCredentials();
    }

    /**
     * Fetches new AWS credentials from Cognito.
     */
    private AwsSessionCredentials fetchNewCredentials(String identityId, String idToken) {
        CognitoIdentityClient cognitoIdentityClient = CognitoIdentityClient.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .build();

        try {
            GetCredentialsForIdentityRequest credentialsRequest = GetCredentialsForIdentityRequest.builder()
                    .identityId(identityId)
                    .logins(Map.of(loginsKey.replace("https://", ""), idToken))
                    .build();

            GetCredentialsForIdentityResponse credentialsResponse = cognitoIdentityClient.getCredentialsForIdentity(credentialsRequest);

            // Debugging response
            System.out.println("Access Key ID: " + credentialsResponse.credentials().accessKeyId());
            System.out.println("Secret Access Key: " + credentialsResponse.credentials().secretKey());
            System.out.println("Session Token: " + credentialsResponse.credentials().sessionToken());
            System.out.println("Credentials Expiration: " + credentialsResponse.credentials().expiration());

            return AwsSessionCredentials.create(
                    credentialsResponse.credentials().accessKeyId(),
                    credentialsResponse.credentials().secretKey(),
                    credentialsResponse.credentials().sessionToken());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch AWS credentials", e);
        }
    }

    /**
     * Extracts the expiration time from the credentials.
     */
    private Instant fetchCredentialsExpiration(AwsSessionCredentials credentials) {
        // Placeholder: Replace with actual logic to extract expiration time
        return Instant.now().plusSeconds(3600); // Example expiration 1 hour from now
    }

    /**
     * Inner class to store cached credentials with expiration time.
     */
    private static class CachedCredentials {
        private final AwsSessionCredentials awsSessionCredentials;
        private final Instant expiration;

        public CachedCredentials(AwsSessionCredentials awsSessionCredentials, Instant expiration) {
            this.awsSessionCredentials = awsSessionCredentials;
            this.expiration = expiration;
        }

        public AwsSessionCredentials getAwsSessionCredentials() {
            return awsSessionCredentials;
        }

        public Instant getExpiration() {
            return expiration;
        }

        public boolean isValid() {
            Instant now = Instant.now();
            System.out.println("Current time: " + now + ", Expiration time: " + expiration);
            return now.isBefore(expiration.minusSeconds(60)); // Add buffer of 60 seconds
        }
    }
}

