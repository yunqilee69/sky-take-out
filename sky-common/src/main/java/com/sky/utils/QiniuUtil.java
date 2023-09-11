package com.sky.utils;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.sky.properties.QiniuProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 七牛云上传工具
 */
@Component
@Slf4j
public class QiniuUtil {

    @Autowired
    private QiniuProperties qiniuProperties;

    /**
     * 上传图片到七牛云，并返回访问连接
     *
     * @param file
     * @param fileName
     * @return
     */
    public String upload(InputStream file, String fileName) {
        //构造一个带指定Region对象的配置类
        Configuration cfg = new Configuration(Region.xinjiapo());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传

        String accessKey = qiniuProperties.getAccessKey();
        String secretKey = qiniuProperties.getSecretKey();
        String bucketName = qiniuProperties.getBucketName();
        //如果是Windows情况下，格式是 D:\\qiniu\\test.png
        String localFilePath = "/home/qiniu/test.png";
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucketName);
        try {
            // 开始上传
            Response response = uploadManager.put(file, fileName, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
        } catch (QiniuException ex) {
            ex.printStackTrace();
        }
        return qiniuProperties.getBaseUrl() + fileName;
    }
}
