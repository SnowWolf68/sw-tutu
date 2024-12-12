package com.snwolf.swtutu.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snwolf.swtutu.model.dto.picture.PictureQueryRequest;
import com.snwolf.swtutu.model.dto.picture.PictureUploadRequest;
import com.snwolf.swtutu.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snwolf.swtutu.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
* @author zhang
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2024-12-11 20:46:42
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     * @param multipartFile 前端提交的图片对象
     * @param pictureUploadRequest 有可能是更新图片请求, 那么request携带图片id
     * @param request 用于获取上传图片的用户
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest,
                            HttpServletRequest request);

    /**
     * 校验图片
     *
     * @param multipartFile
     * @return
     */
    void validatePicture(MultipartFile multipartFile, HttpServletRequest request);

    /**
     * 校验更新的图片是否符合要求
     *
     * @param picture
     */
    void validateUpdatePicture(Picture picture);

    /**
     * 根据id获取图片封装类
     * @param id
     * @return
     */
    PictureVO getPictureVOById(Long id);

    /**
     * 构造查询条件
     *
     * @param pictureQueryRequest
     * @return
     */
    Wrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取分页对象封装类
     * @param picturePage
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage);
}
