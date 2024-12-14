package com.snwolf.swtutu.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snwolf.swtutu.annotation.AuthCheck;
import com.snwolf.swtutu.common.BaseResponse;
import com.snwolf.swtutu.common.ResultUtils;
import com.snwolf.swtutu.constant.UserConstant;
import com.snwolf.swtutu.exception.BusinessException;
import com.snwolf.swtutu.exception.ErrorCode;
import com.snwolf.swtutu.exception.ThrowUtils;
import com.snwolf.swtutu.model.dto.picture.*;
import com.snwolf.swtutu.model.entity.Picture;
import com.snwolf.swtutu.model.entity.User;
import com.snwolf.swtutu.model.enums.PictureReviewStatusEnum;
import com.snwolf.swtutu.model.enums.UserRoleEnum;
import com.snwolf.swtutu.model.vo.PictureVO;
import com.snwolf.swtutu.request.DeleteRequest;
import com.snwolf.swtutu.service.PictureService;
import com.snwolf.swtutu.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 2024/12/12 18:45
 * @Description: 图片相关接口
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @PostMapping("/upload")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> upload(@RequestPart("file") MultipartFile multipartFile,
                                          PictureUploadRequest pictureUploadRequest,
                                          HttpServletRequest request) {
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, request);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过url上传
     *
     * @param fileUrl
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestParam("url") String fileUrl,
                                                       PictureUploadRequest pictureUploadRequest,
                                                       HttpServletRequest request) {
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, request);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(deleteRequest) || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCode.PARAMS_ERROR, "用户未登录");
        Picture oldPicture = pictureService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(ObjectUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        boolean result = pictureService.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新图片 (仅管理员可用)
     *
     * @param pictureUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureUpdateRequest), ErrorCode.PARAMS_ERROR, "参数不能为空");
        // 判断图片是否存在
        Picture oldPicture = pictureService.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(ObjectUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 图片校验
        pictureService.validateUpdatePicture(picture);
        // 改图片的审核状态
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParam(picture, loginUser);
        // 数据库更新
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id) {
        ThrowUtils.throwIf(ObjectUtil.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR, "参数错误");
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(picture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片 (封装类) (所有用户均可调用)
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(Long id) {
        ThrowUtils.throwIf(ObjectUtil.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR, "参数错误");
        // TODO: 这里也要限制用户只能查看已经审核通过的照片
        return ResultUtils.success(pictureService.getPictureVOById(id));
    }

    /**
     * 分页获取图片列表 (仅管理员可用)
     *
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureQueryRequest), ErrorCode.PARAMS_ERROR, "参数错误");
        Page<Picture> pageResult = pictureService.page(new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPicSize()));
        return ResultUtils.success(pageResult);
    }

    /**
     * 分页获取图片列表 (封装类) (所有人都可用)
     *
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureQueryRequest), ErrorCode.PARAMS_ERROR, "参数错误");
        // 限制普通用户只能访问已通过审核的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        Page<Picture> picturePage = pictureService.page(new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize()),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage));
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureEditRequest), ErrorCode.PARAMS_ERROR, "参数错误");
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureEditRequest.getId()) || pictureEditRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "参数错误");
        // dto转实体类
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        // 校验图片
        pictureService.validateUpdatePicture(picture);
        // 更新图片编辑时间
        picture.setEditTime(new DateTime());
        // 查询图片是否存在
        Picture oldPicture = pictureService.getById(picture.getId());
        ThrowUtils.throwIf(ObjectUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 仅本人或管理员可编辑
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue()) && loginUser.getId() != oldPicture.getUserId()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }// 改图片的审核状态
        pictureService.fillReviewParam(picture, loginUser);
        // 更新数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> pictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request){
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureReviewRequest), ErrorCode.PARAMS_ERROR, "参数错误");
        pictureService.doPictureReview(pictureReviewRequest, userService.getLoginUser(request));
        return ResultUtils.success(true);
    }
}
