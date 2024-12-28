package com.sgl.sglscholar.service;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@Service
public class S3Service {

    public void uploadFileToS3(AwsSessionCredentials awsSessionCredentials, String bucketName, File file) {
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(() -> awsSessionCredentials)
                .build();

        // Check if the file already exists
        List<S3Object> existingFiles = listFilesInS3Bucket(awsSessionCredentials, bucketName);
        boolean fileExists = existingFiles.stream()
                .anyMatch(s3Object -> s3Object.key().equals(file.getName()));

        // Modify file name if it exists
        String fileName = file.getName();
        if (fileExists) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            fileName = fileName.replaceFirst("(\\.[^.]+)$", "_" + timestamp + "$1"); // Append timestamp before file extension
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.putObject(putObjectRequest, Paths.get(file.getAbsolutePath()));
    }

    public String generatePresignedUrl(AwsSessionCredentials awsSessionCredentials, String bucketName, String fileName) {
        // Create a presigner instance
        S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(() -> awsSessionCredentials)
                .region(software.amazon.awssdk.regions.Region.US_EAST_1) // Ensure correct region
                .build();

        try {
            // Build the GetObjectRequest
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            // Build the GetObjectPresignRequest with an expiration time
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(60)) // URL valid for 1 hour
                    .getObjectRequest(getObjectRequest)
                    .build();

            // Generate the presigned URL
            return presigner.presignGetObject(presignRequest).url().toString();
        } finally {
            // Close the presigner to release resources
            presigner.close();
        }
    }

    public void deleteFileFromS3(AwsSessionCredentials awsSessionCredentials, String bucketName, String fileName) {
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(() -> awsSessionCredentials)
                .build();

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            System.out.println("Successfully deleted file: " + fileName + " from bucket: " + bucketName);
        } catch (S3Exception e) {
            System.err.println("Failed to delete file: " + fileName);
            System.err.println("Error Message: " + e.awsErrorDetails().errorMessage());
            System.err.println("Status Code: " + e.statusCode());
            System.err.println("AWS Request ID: " + e.requestId());
            throw e; // Rethrow for global error handling
        } catch (Exception e) {
            System.err.println("Unexpected error occurred while deleting file: " + e.getMessage());
            throw new RuntimeException("Delete operation failed", e);
        }
    }


    public void downloadFileFromS3(AwsSessionCredentials awsSessionCredentials, String bucketName, String fileName, String destinationPath) {
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(() -> awsSessionCredentials)
                .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.getObject(getObjectRequest, Paths.get(destinationPath));
    }

    public List<S3Object> listFilesInS3Bucket(AwsSessionCredentials awsSessionCredentials, String bucketName) {
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(() -> awsSessionCredentials)
                .build();

        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .build();

        ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjectsRequest);

        return listObjectsResponse.contents();
    }
}
