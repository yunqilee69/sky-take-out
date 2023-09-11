package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.qiniu")
@Data
public class QiniuProperties {

    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String baseUrl;

}
