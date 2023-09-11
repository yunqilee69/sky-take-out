package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.QiniuUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    
    @Autowired
    private QiniuUtil qiniuUtil;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 获取后缀
            String extension = originalFilename.substring(originalFilename.indexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            String filePath = qiniuUtil.upload(file.getInputStream(), fileName);
            log.info("上传图片成功，路径：{}", filePath);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("上传图片失败：{}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
