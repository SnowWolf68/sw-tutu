package com.snwolf.swtutu.manager.upload.template;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.snwolf.swtutu.configuration.CosClientConfig;
import com.snwolf.swtutu.exception.BusinessException;
import com.snwolf.swtutu.exception.ErrorCode;
import com.snwolf.swtutu.exception.ThrowUtils;
import com.snwolf.swtutu.manager.COSManager;
import com.snwolf.swtutu.model.dto.picture.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/14/2024
 * @Description: 图片文件上传模版对象
 */
@Slf4j
public abstract class PictureFileUploadTemplate {

    @Resource
    private COSManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 图片上传接口, 返回上传图片的信息
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult pictureFileUpload(Object inputSource, String uploadPathPrefix){
        // 生成文件名, 文件名格式: 时间戳 + UUID + 文件后缀
        File file = null;
        String originalFileName = null;
        try {
            ThrowUtils.throwIf(ObjectUtil.isNull(inputSource), ErrorCode.PARAMS_ERROR, "文件不能为空");
            originalFileName = getOriginalFileName(inputSource);
            file = File.createTempFile(originalFileName, null);
            // 图片校验
            validateFile(inputSource);
            // 获取图片文件
            file = getFile(inputSource);
            String fileName = String.format("%s_%s.%s", DateUtil.formatDate(new DateTime()),
                    RandomUtil.randomString(10), FileUtil.getSuffix(originalFileName));
            // 生成文件上传路径: 上传路径前缀 / 文件名
            String filePath = String.format("/%s/%s", uploadPathPrefix, fileName);
            // 上传图片
            PutObjectResult putObjectResult = cosManager.putObjectWithResult(file, filePath);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + filePath);
            uploadPictureResult.setPicHeight(imageInfo.getHeight());
            uploadPictureResult.setPicWidth(imageInfo.getWidth());
            uploadPictureResult.setPicName(originalFileName);
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicScale((double) imageInfo.getWidth() / imageInfo.getHeight());
            return uploadPictureResult;
        } catch (IOException e) {
            log.error("文件上传失败, e = {}", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            if (ObjectUtil.isNotNull(file)) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("临时文件删除失败, filePath = {}", originalFileName);
                }
            }
        }
    }

    protected abstract void validateFile(Object inputSource);

    protected abstract File getFile(Object inputSource);

    protected abstract String getOriginalFileName(Object inputSource);
}
