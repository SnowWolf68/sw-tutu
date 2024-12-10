package com.snwolf.swtutu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snwolf.swtutu.constant.CommonConstant;
import com.snwolf.swtutu.constant.UserConstant;
import com.snwolf.swtutu.exception.ErrorCode;
import com.snwolf.swtutu.exception.ThrowUtils;
import com.snwolf.swtutu.model.dto.user.UserLoginRequest;
import com.snwolf.swtutu.model.dto.user.UserQueryRequest;
import com.snwolf.swtutu.model.dto.user.UserRegisterRequest;
import com.snwolf.swtutu.model.entity.User;
import com.snwolf.swtutu.model.enums.UserRoleEnum;
import com.snwolf.swtutu.model.vo.LoginUserVO;
import com.snwolf.swtutu.model.vo.UserVO;
import com.snwolf.swtutu.service.UserService;
import com.snwolf.swtutu.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author zhang
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-12-09 20:03:27
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        // 1. 校验参数
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 16, ErrorCode.PARAMS_ERROR, "用户账号长度在4-16之间");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码长度不能小于8");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        // 2. 检查用户账号是否和数据库中已有的重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        Long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");
        // 3. 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        // 设置用户默认名称
        user.setUserName("用户" + userAccount);
        // 设置用户默认权限
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = save(user);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败");
        return user.getId();
    }

    /**
     * 密码加密
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 加盐
        final String SALT = "snwolf";
        return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes());
    }

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 1. 校验
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword()), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 16, ErrorCode.PARAMS_ERROR, "用户账号长度在4-16之间");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码长度大于8");
        // 2. 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询数据库中的用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 不存在, 抛异常
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        // 保存用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 4. 返回当前登录成功的用户
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        Long currentUserId = currentUser.getId();
        ThrowUtils.throwIf(ObjectUtil.isNull(currentUser) || ObjectUtil.isNull(currentUserId), ErrorCode.NOT_LOGIN_ERROR);
        // 从数据库中查询, 避免session中的缓存不一致
        currentUser = this.getById(currentUserId);
        ThrowUtils.throwIf(ObjectUtil.isNull(currentUser), ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 从session中判断用户是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(ObjectUtil.isNull(userObj), ErrorCode.NOT_LOGIN_ERROR);
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(userQueryRequest), ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(userQueryRequest.getId()), "id", userQueryRequest.getId());
        queryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserName()), "user_name", userQueryRequest.getUserName());
        queryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserAccount()), "user_account", userQueryRequest.getUserAccount());
        queryWrapper.eq(StrUtil.isNotBlank(userQueryRequest.getUserRole()), "user_role", userQueryRequest.getUserRole());
        queryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserProfile()), "user_profile", userQueryRequest.getUserProfile());
        queryWrapper.orderBy(StrUtil.isNotBlank(userQueryRequest.getSortField()), userQueryRequest.getSortOrder().equals(CommonConstant.SORT_ORDER_ASC), userQueryRequest.getSortField());
        return queryWrapper;
    }
}




