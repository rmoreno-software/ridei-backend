package com.ridei.identity.infrastructure.adapter.out.storage;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ridei.identity.domain.model.ImageContentType;
import com.ridei.identity.domain.model.PresignedUpload;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.port.out.ProfilePictureStoragePort;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class CloudflareR2StorageAdapter implements ProfilePictureStoragePort {

    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(5);

    private final S3Presigner presigner;
    private final String bucket;
    private final String publicBaseUrl;

    public CloudflareR2StorageAdapter(
        @Value("${cloudflare.r2.account-id}") String accountId,
        @Value("${cloudflare.r2.access-key}") String accessKey,
        @Value("${cloudflare.r2.secret-key}") String secretKey,
        @Value("${cloudflare.r2.bucket}") String bucket,
        @Value("${cloudflare.r2.public-base-url}") String publicBaseUrl
    ) {
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
        this.presigner = S3Presigner.builder()
            .region(Region.of("auto"))
            .endpointOverride(URI.create("https://" + accountId + "r2.cloudflarestorage.com"))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)))
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .build();
    }

    @Override
    public PresignedUpload createUploadUrl(
        UserId userId,
        ImageContentType contentType
    ) {
        String key = "profile-pictures/%s/%s./%s".formatted(
            userId.value(), UUID.randomUUID(), contentType.extension()
        );

        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType.value())
            .build();
        
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(UPLOAD_URL_TTL)
            .putObjectRequest(putRequest)
            .build();
        
        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);

        return new PresignedUpload(
            presigned.url().toString(),
            publicBaseUrl + "/" + key,
            Instant.now().plus(UPLOAD_URL_TTL)
        );
    }
    
}
