package com.magenta.customers.infrastructure.adapter.out.storage;

import com.magenta.customers.domain.port.out.DocumentStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3DocumentStorageAdapter implements DocumentStoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${magenta.storage.s3-bucket}")
    private String bucket;

    @Value("${magenta.storage.kms-key-arn:}")
    private String kmsKeyArn;

    @Override
    public String upload(String key, InputStream data, long sizeBytes, String mimeType) {
        PutObjectRequest.Builder reqBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(mimeType)
                .contentLength(sizeBytes);

        if (kmsKeyArn != null && !kmsKeyArn.isBlank()) {
            reqBuilder.serverSideEncryption(ServerSideEncryption.AWS_KMS)
                    .ssekmsKeyId(kmsKeyArn);
        } else {
            reqBuilder.serverSideEncryption(ServerSideEncryption.AES256);
        }

        s3Client.putObject(reqBuilder.build(), RequestBody.fromInputStream(data, sizeBytes));
        String uri = "s3://" + bucket + "/" + key;
        log.debug("Uploaded document to {}", uri);
        return uri;
    }

    @Override
    public String presignedDownloadUrl(String key, Duration ttl) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(r -> r.bucket(bucket).key(key))
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void delete(String key) {
        // key puede venir como s3://bucket/path o como path directo
        String actualKey = key.startsWith("s3://") ? key.substring(key.indexOf('/', 5) + 1) : key;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(actualKey)
                    .build());
            log.info("Deleted S3 object: {}", actualKey);
        } catch (Exception e) {
            log.warn("Failed to delete S3 object {}: {}", actualKey, e.getMessage());
        }
    }
}
