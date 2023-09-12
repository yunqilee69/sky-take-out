package com.sky.service;

import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;

import java.io.InputStream;

public interface QiniuOssService {

    /**
     * 获取七牛云地点配置，项目使用的是新加坡
     * @return
     */
    public Configuration getConfiguration();

    /**
     * 认证信息实例
     * @return
     */
    public Auth getAuth();

    /**
     * 上传图片到七牛云
     * @param inputStream
     * @param fileName
     * @return
     */
    public String upload(InputStream file, String fileName);

    /**
     * 判断照片是否在七牛云中
     *
     * @param key
     * @return
     */
    public FileInfo checkImage(String key);

    /**
     * 删除七牛云存放的照片
     *
     * @param image
     */
    public void deleteImage(String image);
}
