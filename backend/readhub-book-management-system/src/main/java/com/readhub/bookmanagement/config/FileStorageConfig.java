package com.readhub.bookmanagement.config;

import com.readhub.bookmanagement.service.CloudinaryFileStorageService;
import com.readhub.bookmanagement.service.FileStorageService;
import com.readhub.bookmanagement.service.LocalFileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {

    @Value("${app.storage.type:local}")
    private String storageType;

    @Value("${app.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${app.cloudinary.api-key:}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${app.server.base-url:http://localhost:8080}")
    private String serverBaseUrl;

    @Bean
    public FileStorageService fileStorageService() {
        if ("cloudinary".equalsIgnoreCase(storageType) && !cloudName.isEmpty() && !apiKey.isEmpty() && !apiSecret.isEmpty()) {
            return new CloudinaryFileStorageService(cloudName, apiKey, apiSecret);
        }
        return new LocalFileStorageService(serverBaseUrl);
    }
}

