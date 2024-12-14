package com.snwolf.swtutu.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.snwolf.swtutu.exception.BusinessException;
import com.snwolf.swtutu.exception.ErrorCode;
import com.snwolf.swtutu.exception.ThrowUtils;
import com.snwolf.swtutu.manager.upload.template.PictureFileUploadTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/14/2024
 * @Description: URL 文件上传
 */
@Service
public class UrlFilePictureUpload extends PictureFileUploadTemplate {

    @Override
    protected void validateFile(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 图片非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "图片url不能为空");
        // 校验url格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片url格式不正确");
        }
        // 校验url的协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "图片url协议不正确");
        // 发送 HEAD 请求验证文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (!response.isOk()) {
                // 有些服务器不支持HEAD请求, 不应该抛异常, 可以直接返回
                return;
            }
            // 文件存在, 校验文件类型
            String contentType = response.header("Content-Type");
            // 只有contentType不为空才进行判断
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_FORMAT_LIST = Arrays.asList("image/jpeg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(contentType), ErrorCode.PARAMS_ERROR, "图片类型不正确");
            }
            // 文件存在, 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            long contentLength = Long.parseLong(contentLengthStr);

        } finally {
            // 释放response资源
            if (ObjectUtil.isNotNull(response)) {
                response.close();
            }
        }
    }

    @Override
    protected File getFile(Object inputSource) {
        String fileUrl = (String) inputSource;
        File file = null;
        try {
            file = File.createTempFile(getOriginalFileName(inputSource), null);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误");
        }
        HttpUtil.downloadFile(fileUrl, file);
        return file;
    }

    @Override
    protected String getOriginalFileName(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }
}
