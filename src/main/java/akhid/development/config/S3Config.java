package akhid.development.config;


import org.apache.camel.Configuration;
import org.jboss.logging.Logger;

import io.minio.MinioClient;

@Configuration
public class S3Config {
    private static final Logger LOGGER = Logger.getLogger("ListenerMinioClient");

    public MinioClient minioClient(S3ConfigProperties properties){
        LOGGER.info("minio url...:"+properties.url());

        return MinioClient.builder()
                .endpoint(properties.url())
                .credentials(properties.accessKey(), properties.secretPass())
                .build();
    }
}
