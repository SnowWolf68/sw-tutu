package com.snwolf.swtutu.controller;

import cn.hutool.core.util.ObjectUtil;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.snwolf.swtutu.annotation.AuthCheck;
import com.snwolf.swtutu.common.BaseResponse;
import com.snwolf.swtutu.common.ResultUtils;
import com.snwolf.swtutu.constant.UserConstant;
import com.snwolf.swtutu.exception.BusinessException;
import com.snwolf.swtutu.exception.ErrorCode;
import com.snwolf.swtutu.manager.COSManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/11/2024
 * @Description: 文件相关接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private COSManager cosManager;

    @GetMapping("/test/download")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> testDownLoadFile(String filePath, HttpServletResponse response) throws IOException {
        COSObjectInputStream objectContent = null;
        try {
            COSObject object = cosManager.getObject(filePath);
            objectContent = object.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(objectContent);
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filePath);

            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("文件下载失败, e = {}, filePath = {}", e, filePath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件下载失败");
        } finally {
            if (ObjectUtil.isNotNull(objectContent)) {
                objectContent.close();
            }
        }
        return ResultUtils.success(true);
    }

    /**
     * 文件上传
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String fileName = null;
        File file = null;
        try {
            fileName = multipartFile.getOriginalFilename();
            String filePath = "/test/" + fileName;
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(file, filePath);
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败, fileName = {}, e = {}", fileName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            // 删除临时文件
            if (ObjectUtil.isNotNull(file)) {
                boolean deleteResult = file.delete();
                if(!deleteResult){
                    log.error("临时文件删除失败, filePath = {}", fileName);
                }
            }
        }
    }
}
