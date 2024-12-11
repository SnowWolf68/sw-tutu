package com.snwolf.swtutu.controller;

import cn.hutool.core.util.ObjectUtil;
import com.snwolf.swtutu.annotation.AuthCheck;
import com.snwolf.swtutu.common.BaseResponse;
import com.snwolf.swtutu.common.ResultUtils;
import com.snwolf.swtutu.exception.BusinessException;
import com.snwolf.swtutu.exception.ErrorCode;
import com.snwolf.swtutu.manager.COSManager;
import com.snwolf.swtutu.model.enums.UserRoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/11/2024
 * @Description: 文件上传接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private COSManager cosManager;

    @AuthCheck(mustRole = "admin")
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file")MultipartFile multipartFile){
        String filename = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", filename);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败, filePath = {}, e = {}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if(ObjectUtil.isNotNull(file)){
                boolean delete = file.delete();
                if(!delete){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除临时文件失败");
                }
            }
        }
    }
}
