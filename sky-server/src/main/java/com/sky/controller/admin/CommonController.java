package com.sky.controller.admin;


//        1.获取原始文件名
//        2.截取文件名的后缀
//        3.通过UUID拼接后缀构建新的文件名称
//        4.通过aliOssUtil 传入文件字节数组和新的文件名称,返回图片路径

import com.sky.constant.MessageConstant;
import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RequestMapping("/admin/common")
@RestController
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("文件上传：{}", file.getOriginalFilename());
        try {
            // 1.获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 2.截取文件名的后缀
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 3.通过UUID拼接后缀构建新的文件名称
            String fileName = UUID.randomUUID().toString() + suffix;
            // 4.通过aliOssUtil 传入文件字节数组和新的文件名称,返回图片路径
            String url = aliOssUtil.upload(file.getBytes(), fileName);
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage());
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}
