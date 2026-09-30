package com.ridei.garage.infrastructure.adapter.out.storage;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ridei.garage.domain.model.ImageContentType;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.model.PresignedUpload;
import com.ridei.garage.domain.port.out.MotorbikePhotoStoragePort;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component 
public class CloudflareR2MotorbikePhotoStorageAdapter implements MotorbikePhotoStoragePort {

    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(5);

    private final S3Presigner presigner;
    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public CloudflareR2MotorbikePhotoStorageAdapter(
        @Value("${cloudflare.r2.account-id}") String accountId,
        @Value("${cloudflare.r2.access-key}") String accessKey,
        @Value("${cloudflare.r2.secret-key}") String secretKey,
        @Value("${cloudflare.r2.bucket}") String bucket,
        @Value("${cloudflare.r2.public-base-url}") String publicBaseUrl
    ) {
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
        URI endpoint = URI.create("https://" + accountId + ".r2.cloudflarestorage.com");
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)
        );

        this.presigner = S3Presigner.builder()
            .region(Region.of("auto"))
            .endpointOverride(endpoint)
            .credentialsProvider(credentials)
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .build();

        this.s3Client = S3Client.builder()
            .region(Region.of("auto"))
            .endpointOverride(endpoint)
            .credentialsProvider(credentials)
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .httpClient(UrlConnectionHttpClient.create())
            .build();
    }

    private String prefixFor(
        OwnerId ownerId,
        MotorbikeId motorbikeId
    ) {
        return "garage/%s/%s/".formatted(ownerId.value(), motorbikeId.value());
    }

    @Override
    public PresignedUpload createUploadUrl(OwnerId ownerId, MotorbikeId motorbikeId, ImageContentType contentType) {
        String key = prefixFor(ownerId, motorbikeId) + UUID.randomUUID() + "." + contentType.extension();

        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType.value())
            .build();
        
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(UPLOAD_URL_TTL)
            .putObjectRequest(putRequest)
            .build();
        
        var presigned = presigner.presignPutObject(presignRequest);

        return new PresignedUpload(
            presigned.url().toString(),
            publicBaseUrl + "/" + key,
            Instant.now().plus(UPLOAD_URL_TTL)
        );  
    }

    @Override
    public boolean belongsToMotorbike(OwnerId ownerId, MotorbikeId motorbikeId, String publicUrl) {
        if (publicUrl == null) return false;
        String expectedPrefix = publicBaseUrl + "/" +prefixFor(ownerId, motorbikeId);
        return publicUrl.startsWith(expectedPrefix);
    }

    @Override
    public long getContentLength(String publicUrl) {
        String key = publicUrl.replace(publicBaseUrl + "/", "");
        return s3Client.headObject(HeadObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build())
            .contentLength();
    }

    @Override
    public void delete(String publicUrl) {
        String key = publicUrl.replace( publicBaseUrl + "/", "");
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build());
    }
    
}
