package com.snwolf.swtutu.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snwolf.swtutu.annotation.AuthCheck;
import com.snwolf.swtutu.common.BaseResponse;
import com.snwolf.swtutu.common.ResultUtils;
import com.snwolf.swtutu.constant.UserConstant;
import com.snwolf.swtutu.exception.ErrorCode;
import com.snwolf.swtutu.exception.ThrowUtils;
import com.snwolf.swtutu.model.dto.user.*;
import com.snwolf.swtutu.model.entity.User;
import com.snwolf.swtutu.model.vo.LoginUserVO;
import com.snwolf.swtutu.model.vo.UserVO;
import com.snwolf.swtutu.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/testAuthCheck")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> testAuthCheck() {
        return ResultUtils.success(true);
    }

    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(userRegisterRequest), ErrorCode.PARAMS_ERROR, "参数不能为空");
        long userId = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(userId);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(userLoginRequest), ErrorCode.PARAMS_ERROR, "参数不能为空");
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest, request);
        return ResultUtils.success(loginUserVO);
    }

    @PostMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        LoginUserVO loginUserVO = userService.getLoginUserVO(userService.getLoginUser(request));
        return ResultUtils.success(loginUserVO);
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> add(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(userAddRequest), ErrorCode.PARAMS_ERROR, "参数不能为空");
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        final String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(userService.getEncryptPassword(DEFAULT_PASSWORD));
        return ResultUtils.success(userService.save(user));
    }

    /**
     * 根据id获取用户 (仅管理员)
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "id不能小于0");
        User user = userService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取用户包装类
     * @param id
     * @return
     */
    @GetMapping("/vo/get")
    public BaseResponse<UserVO> getUserVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "id不能小于0");
        User user = userService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ResultUtils.success(userService.getUserVO(user));
    }

    @DeleteMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "id不能小于0");
        return ResultUtils.success(userService.removeById(id));
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(userUpdateRequest), ErrorCode.PARAMS_ERROR, "参数不能为空");
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(userQueryRequest), ErrorCode.PARAMS_ERROR, "参数不能为空");
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size);
        List<UserVO> userVOList = userPage.getRecords().stream().map(userService::getUserVO).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
