package com.freediving.service;

import com.freediving.config.CosProperties;
import com.freediving.dto.UploadResponse;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.CannedAccessControlList;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CosStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "video/mp4",
            "video/quicktime"
    );

    private final CosProperties properties;

    public CosStorageService(CosProperties properties) {
        this.properties = properties;
    }

    public UploadResponse upload(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("unsupported file type: " + file.getContentType());
        }
        if (!StringUtils.hasText(properties.secretId())
                || !StringUtils.hasText(properties.secretKey())
                || !StringUtils.hasText(properties.region())
                || !StringUtils.hasText(properties.bucketName())) {
            throw new IllegalStateException("COS config is incomplete. Required: COS_SECRET_ID, COS_SECRET_KEY, COS_REGION, COS_BUCKET_NAME");
        }

        String objectKey = buildObjectKey(file.getOriginalFilename(), folder);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        COSClient client = createClient();
        try {
            PutObjectRequest request = new PutObjectRequest(properties.bucketName(), objectKey, file.getInputStream(), metadata);
            if (properties.publicRead()) {
                request.setCannedAcl(CannedAccessControlList.PublicRead);
            }
            client.putObject(request);
        } catch (CosServiceException exception) {
            throw new IllegalStateException("COS upload failed. status=%s, code=%s, message=%s, requestId=%s"
                    .formatted(
                            exception.getStatusCode(),
                            exception.getErrorCode(),
                            exception.getErrorMessage(),
                            exception.getRequestId()
                    ), exception);
        } catch (CosClientException exception) {
            throw new IllegalStateException("COS upload failed. clientMessage=%s".formatted(exception.getMessage()), exception);
        } finally {
            client.shutdown();
        }

        return new UploadResponse(resolveUrl(objectKey), objectKey);
    }

    private COSClient createClient() {
        COSCredentials credentials = new BasicCOSCredentials(properties.secretId(), properties.secretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(properties.region()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(credentials, clientConfig);
    }

    private String buildObjectKey(String originalFilename, String folder) {
        String extension = "";
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        String safeFolder = StringUtils.hasText(folder) ? folder.replaceAll("[^a-zA-Z0-9/_-]", "") : "uploads";
        return "%s/%s/%s%s".formatted(safeFolder, LocalDate.now(), UUID.randomUUID(), extension);
    }

    private String resolveUrl(String objectKey) {
        if (StringUtils.hasText(properties.publicBaseUrl())) {
            return properties.publicBaseUrl().replaceAll("/$", "") + "/" + objectKey;
        }
        return "https://%s.cos.%s.myqcloud.com/%s".formatted(properties.bucketName(), properties.region(), objectKey);
    }
}
