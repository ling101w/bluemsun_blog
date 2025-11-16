package com.bluemsun.blog.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    private String endpoint;
    private String bucket;
    private String accessKeyId;
    private String accessKeySecret;

    /**
     * 可选：自定义访问域名（如 CDN 域名或 bucket 域名）。
     * 若为空，将使用 https://{bucket}.{endpoint}
     */
    private String publicDomain;

    /**
     * 付费资源是否使用临时签名 URL（建议开启）。
     */
    private boolean useSignedUrlForPaid = true;

    /**
     * 临时签名 URL 有效分钟数。
     */
    private int signedUrlExpireMinutes = 10;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getPublicDomain() {
        return publicDomain;
    }

    public void setPublicDomain(String publicDomain) {
        this.publicDomain = publicDomain;
    }

    public boolean isUseSignedUrlForPaid() {
        return useSignedUrlForPaid;
    }

    public void setUseSignedUrlForPaid(boolean useSignedUrlForPaid) {
        this.useSignedUrlForPaid = useSignedUrlForPaid;
    }

    public int getSignedUrlExpireMinutes() {
        return signedUrlExpireMinutes;
    }

    public void setSignedUrlExpireMinutes(int signedUrlExpireMinutes) {
        this.signedUrlExpireMinutes = signedUrlExpireMinutes;
    }
}


