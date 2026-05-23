package com.example.meanhwa_back.storage.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/** 이미지 저장소 설정을 바인딩하고 운영 S3 필수값 누락을 기동 시점에 검증한다. */
@Validated
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {
    @NotBlank
    @Pattern(regexp = "fake|s3", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String type = "fake";

    @Positive
    private long maxFileSizeBytes = 5 * 1024 * 1024;

    @NotEmpty
    private List<@NotBlank String> allowedContentTypes = List.of("image/jpeg", "image/png", "image/webp");

    @NotBlank
    private String fakeBaseUrl = "https://fake.meanhwa.local/uploads";

    @Valid
    private S3 s3 = new S3();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }

    public String getFakeBaseUrl() {
        return fakeBaseUrl;
    }

    public void setFakeBaseUrl(String fakeBaseUrl) {
        this.fakeBaseUrl = fakeBaseUrl;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(S3 s3) {
        this.s3 = s3;
    }

    @AssertTrue(message = "S3 storage requires bucket, region, and public-base-url.")
    public boolean isS3ConfiguredWhenEnabled() {
        if (!"s3".equalsIgnoreCase(type)) {
            return true;
        }
        return s3 != null
                && hasText(s3.bucket)
                && hasText(s3.region)
                && hasText(s3.publicBaseUrl);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /** S3 저장소에 필요한 bucket, region, 공개 URL 설정. */
    public static class S3 {
        private String bucket;
        private String region;
        private String publicBaseUrl;

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }
    }
}
