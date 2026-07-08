package com.example.fileupload;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileStorageService {

    private final Storage storage;

    private final String bucketName;

    public FileStorageService(Storage storage, @Value("${gcp.bucket.name}") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    public Map<String, String> generatePreSignedUploadUrl(String filename, String contentType) {
        String fileId = UUID.randomUUID().toString();
        String objectName = fileId + "-" + filename;

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();

        Map<String, String> extensionHeaders = new HashMap<>();
        extensionHeaders.put("Content-Type", contentType);
        URL url = storage.signUrl(
                blobInfo,
                15,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withExtHeaders(extensionHeaders),
                Storage.SignUrlOption.withV4Signature()
        );

        return Map.of(
                "uploadUrl", url.toString(),
                "fileId", fileId,
                "objectName", objectName
        );
    }
}
