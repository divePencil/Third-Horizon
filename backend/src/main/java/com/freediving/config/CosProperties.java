package com.freediving.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cos")
public record CosProperties(
        String secretId,
        String secretKey,
        String region,
        String bucketName,
        String publicBaseUrl,
        boolean publicRead
) {
}
