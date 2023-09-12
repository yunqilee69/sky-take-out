package com.sky.service.impl;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.sky.properties.QiniuOssProperties;
import com.sky.service.QiniuOssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 七牛云上传和删除操作
 */
@Service
@Slf4j
public class QiniuOssServiceImpl implements QiniuOssService {

    @Autowired
    private QiniuOssProperties qiniuOssProperties;

    /**
     * 获取七牛云地点配置，项目使用的是新加坡
     * @return
     */
    @Override
    public Configuration getConfiguration() {
        //构造一个带指定Region对象的配置类，使用新加坡
        Configuration cfg = new Configuration(Region.xinjiapo());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        return cfg;
    }

    /**
     * 认证信息实例
     * @return
     */
    public Auth getAuth() {
        return Auth.create(qiniuOssProperties.getAccessKey(), qiniuOssProperties.getSecretKey());
    }

    /**
     * 上传图片到七牛云
     * @param file
     * @param fileName
     * @return
     */
    public String upload(InputStream file, String fileName){
        Configuration cfg = getConfiguration();
        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传

        String bucketName = qiniuOssProperties.getBucketName();

        Auth auth = getAuth();
        String upToken = auth.uploadToken(bucketName);
        try {
            // 开始上传
            Response response = uploadManager.put(file, fileName, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
        } catch (QiniuException ex) {
            ex.printStackTrace();
        }
        return qiniuOssProperties.getBaseUrl() + fileName;
    }

    /**
     * 判断照片是否在七牛云中
     *
     * @param key
     * @return
     */
    public FileInfo checkImage(String key){
        FileInfo fileInfo = null;
        try {
            BucketManager bucketManager = new BucketManager(getAuth(), getConfiguration());
            String bucketName = qiniuOssProperties.getBucketName();
            fileInfo = bucketManager.stat(bucketName, key);
        } catch (QiniuException ignored) {
        }
        return fileInfo;
    }

    /**
     * 删除七牛云存放的照片
     * @param image
     * @return
     */
    public Boolean deleteImage(String image){
        // 从image中截取出文件的名称,使用正则表达式去截取文件名
        // 例如http://img.clueli.top/bc9f7bc1-8859-416f-ae6e-a388678bc835.png截取的结果为bc9f7bc1-8859-416f-ae6e-a388678bc835.png
        String pattern = ".*/([^/]+)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(image);
        String key = "";
        if (!m.find()) {
            return false;
        }
        key = m.group(1);

        if (Objects.isNull(checkImage(key))) {
            return true;
        } else {
            try {
                BucketManager bucketManager = new BucketManager(getAuth(), getConfiguration());
                String bucketName = qiniuOssProperties.getBucketName();
                bucketManager.delete(bucketName, key);
                return true;
            } catch (QiniuException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
