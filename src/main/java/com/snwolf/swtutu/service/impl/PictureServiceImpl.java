package com.snwolf.swtutu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snwolf.swtutu.constant.CommonConstant;
import com.snwolf.swtutu.constant.UserConstant;
import com.snwolf.swtutu.exception.ErrorCode;
import com.snwolf.swtutu.exception.ThrowUtils;
import com.snwolf.swtutu.manager.FileManager;
import com.snwolf.swtutu.model.dto.picture.PictureQueryRequest;
import com.snwolf.swtutu.model.dto.picture.PictureReviewRequest;
import com.snwolf.swtutu.model.dto.picture.PictureUploadRequest;
import com.snwolf.swtutu.model.dto.picture.UploadPictureResult;
import com.snwolf.swtutu.model.entity.Picture;
import com.snwolf.swtutu.model.entity.User;
import com.snwolf.swtutu.model.enums.PictureReviewStatusEnum;
import com.snwolf.swtutu.model.vo.PictureVO;
import com.snwolf.swtutu.model.vo.UserVO;
import com.snwolf.swtutu.service.PictureService;
import com.snwolf.swtutu.mapper.PictureMapper;
import com.snwolf.swtutu.service.UserService;
import com.snwolf.swtutu.utils.SFunctionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhang
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-11 20:46:42
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private UserService userService;

    @Resource
    private FileManager fileManager;

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest,
                                   HttpServletRequest request) {
        // 校验图片
        validatePicture(multipartFile, request);
        // 判断是新增图片还是修改图片
        User loginUser = userService.getLoginUser(request);
        Long picId = pictureUploadRequest.getId();
        if (ObjectUtil.isNotNull(picId)) {
            // 修改请求, 首先判断图片是否存在
            Picture picture = getById(picId);
            ThrowUtils.throwIf(ObjectUtil.isNull(picture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 改图片的审核状态
            fillReviewParam(picture, loginUser);
        }
        // 构造数据库Picture对象
        String uploadPathPrefix = "public/" + loginUser.getId();
        UploadPictureResult uploadPictureResult = fileManager.pictureFileUpload(multipartFile, uploadPathPrefix);

        Picture picture = new Picture();
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setUserId(loginUser.getId());
        if (ObjectUtil.isNotNull(pictureUploadRequest) && ObjectUtil.isNotNull(pictureUploadRequest.getId())) {
            picture.setId(pictureUploadRequest.getId());
            picture.setEditTime(new DateTime());
            updateById(picture);
        } else {
            save(picture);
        }
        PictureVO pictureVO = PictureVO.objToVO(picture);
        pictureVO.setUser(UserVO.objToVO(loginUser));
        return pictureVO;
    }

    public void validatePicture(MultipartFile multipartFile, HttpServletRequest request) {
        // 只有登录用户才有权限上传
        ThrowUtils.throwIf(ObjectUtil.isNull(userService.getLoginUser(request)), ErrorCode.NO_AUTH_ERROR, "未登录");
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
    public void validateUpdatePicture(Picture picture) {
        // 校验id不能为空
        ThrowUtils.throwIf(ObjectUtil.isNull(picture.getId()), ErrorCode.PARAMS_ERROR, "id不能为空");
        // 校验url不能过长
        ThrowUtils.throwIf(ObjectUtil.isNotEmpty(picture.getUrl()) && picture.getUrl().length() > 512, ErrorCode.PARAMS_ERROR, "url不能过长");
        // 校验introduction不能过长
        ThrowUtils.throwIf(ObjectUtil.isNotEmpty(picture.getIntroduction()) && picture.getIntroduction().length() > 512, ErrorCode.PARAMS_ERROR, "introduction不能过长");
    }

    @Override
    public PictureVO getPictureVOById(Long id) {
        Picture picture = getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(picture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        PictureVO pictureVO = PictureVO.objToVO(picture);
        // 填充user
        User user = userService.getById(picture.getUserId());
        UserVO userVO = UserVO.objToVO(user);
        pictureVO.setUser(userVO);
        return pictureVO;
    }

    @Override
    public Wrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        LambdaQueryWrapper<Picture> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 先拼接两类特殊的查询条件
        // 1. 从searchText中多字段查询
        // 这里需要注意, searchText中的内容所要查询的两个字段 name, introduction , 我们要将这两个like放在一个and里面拼接到sql中, 因此外面需要套一个and
        lambdaQueryWrapper.and(ObjectUtil.isNotEmpty(pictureQueryRequest.getSearchText()), wrapper ->
                wrapper.like(Picture::getName, pictureQueryRequest.getSearchText())
                        .or()
                        .like(Picture::getIntroduction, pictureQueryRequest.getSearchText())
        );
        // 2. 从tags中查询
        if (CollUtil.isNotEmpty(pictureQueryRequest.getTags())) {
            // 这里为了精确查询到某一个tag, 因此在tag的两边拼接上 双引号 "" , 这样就能够匹配到json数组中的某一个tag
            lambdaQueryWrapper.like(Picture::getTags, "\"" + pictureQueryRequest.getTags() + "\"");
        }
        // 从剩余字段中查询
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(pictureQueryRequest.getId()), Picture::getId, pictureQueryRequest.getId());
        lambdaQueryWrapper.like(ObjectUtil.isNotNull(pictureQueryRequest.getName()), Picture::getName, pictureQueryRequest.getName());
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(pictureQueryRequest.getIntroduction()), Picture::getIntroduction, pictureQueryRequest.getIntroduction());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(pictureQueryRequest.getCategory()), Picture::getCategory, pictureQueryRequest.getCategory());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(pictureQueryRequest.getPicSize()), Picture::getPicSize, pictureQueryRequest.getPicSize());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(pictureQueryRequest.getPicWidth()), Picture::getPicWidth, pictureQueryRequest.getPicWidth());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(pictureQueryRequest.getPicHeight()), Picture::getPicHeight, pictureQueryRequest.getPicHeight());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(pictureQueryRequest.getPicScale()), Picture::getPicScale, pictureQueryRequest.getPicScale());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(pictureQueryRequest.getPicFormat()), Picture::getPicFormat, pictureQueryRequest.getPicFormat());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(pictureQueryRequest.getUserId()), Picture::getUserId, pictureQueryRequest.getUserId());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(pictureQueryRequest.getReviewStatus()), Picture::getReviewStatus, pictureQueryRequest.getReviewStatus());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(pictureQueryRequest.getReviewerId()), Picture::getReviewerId, pictureQueryRequest.getReviewerId());
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(pictureQueryRequest.getReviewMessage()), Picture::getReviewMessage, pictureQueryRequest.getReviewMessage());
        lambdaQueryWrapper.orderBy(ObjectUtil.isNotEmpty(pictureQueryRequest.getSortField()) && ObjectUtil.isNotEmpty(pictureQueryRequest.getSortOrder()),
                pictureQueryRequest.getSortOrder().equals(CommonConstant.SORT_ORDER_ASC),
                SFunctionUtils.getSFunction(Picture.class, pictureQueryRequest.getSortField()));
        return lambdaQueryWrapper;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage) {
        if(picturePage.getRecords().isEmpty()){
            return new Page<>(picturePage.getCurrent(), picturePage.getSize());
        }
        // 获取所有相关的userId, 统一查询user对象
        Set<Long> userIdList = picturePage.getRecords().stream().map(Picture::getUserId).collect(Collectors.toSet());
        List<User> users = userService.listByIds(userIdList);

        Map<Long, List<UserVO>> userVOMap = users.stream().map(UserVO::objToVO).collect(Collectors.groupingBy(UserVO::getId));
        List<PictureVO> pictureVOList = picturePage.getRecords().stream().map(PictureVO::objToVO).collect(Collectors.toList());
        pictureVOList.forEach(pictureVO -> pictureVO.setUser(userVOMap.get(pictureVO.getUserId()).get(0)));

        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureReviewRequest), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(!isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无操作权限");
        // 判断图片是否存在
        Picture picture = getById(pictureReviewRequest.getId());
        ThrowUtils.throwIf(ObjectUtil.isNull(picture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 校验审核状态是否重复
        ThrowUtils.throwIf(picture.getReviewStatus().equals(pictureReviewRequest.getReviewStatus()), ErrorCode.OPERATION_ERROR, "审核状态重复");
        // 设置审核员id
        pictureReviewRequest.setReviewerId(loginUser.getId());
        // 数据库操作
        Picture newPicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, newPicture);
        updateById(newPicture);
    }

    @Override
    public boolean isAdmin(User user){
        return ObjectUtil.isNotNull(user) && user.getUserRole().equals(UserConstant.ADMIN_ROLE);
    }


    @Override
    public void fillReviewParam(Picture picture, User loginUser){
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCode.PARAMS_ERROR, "用户未登录");
        if(isAdmin(loginUser)){
            // 管理员自动通过
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动通过");
        }else{
            // 普通用户默认为待审核状态
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }
}




