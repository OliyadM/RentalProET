package com.rentalpro.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    @ConditionalOnProperty(name = "cloudinary.enabled", havingValue = "true", matchIfMissing = false)
    public Cloudinary cloudinary() {
        // Only create if credentials are provided
        if (cloudName == null || cloudName.isEmpty() || cloudName.equals("your-cloud-name")) {
            // Return a dummy instance instead of null to avoid bean creation issues
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", "dummy",
                    "api_key", "dummy",
                    "api_secret", "dummy"
            ));
        }
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}
