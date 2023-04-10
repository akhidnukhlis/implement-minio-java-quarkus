package akhid.development.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "minio")
public interface S3ConfigProperties {
    String url();
    // Method names in camelCase map to kebab-case property names
    String accessKey();
    String secretPass();
    String bucket();
}
