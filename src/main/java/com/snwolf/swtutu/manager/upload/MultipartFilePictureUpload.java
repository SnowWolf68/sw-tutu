package com.snwolf.swtutu.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.snwolf.swtutu.exception.BusinessException;
import com.snwolf.swtutu.exception.ErrorCode;
import com.snwolf.swtutu.exception.ThrowUtils;
import com.snwolf.swtutu.manager.upload.template.PictureFileUploadTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/14/2024
 * @Description: MultipartFile 类型图片上传
 */
@Service
@Slf4j
public class MultipartFilePictureUpload extends PictureFileUploadTemplate {

    @Override
    protected void validateFile(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(ObjectUtil.isNull(multipartFile), ErrorCode.PARAMS_ERROR, "上传图片不能为空");
        // 图片大小不能超过 2MB
        final int SIZE = 1024 * 1024 * 2;
        ThrowUtils.throwIf(multipartFile.getSize() > SIZE, ErrorCode.PARAMS_ERROR, "上传图片大小不能超过 2MB");
        // 校验文件后缀
        String fileName = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(fileName);
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "上传图片格式不正确");
    }

    @Override
    protected File getFile(Object inputSource) {
        try {
            MultipartFile multipartFile = (MultipartFile) inputSource;
            File file = File.createTempFile(getOriginalFileName(inputSource), null);
            multipartFile.transferTo(file);
            return file;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误");
        }
    }

    @Override
    protected String getOriginalFileName(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }
}
