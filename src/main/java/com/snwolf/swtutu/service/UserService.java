package com.snwolf.swtutu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.snwolf.swtutu.model.dto.user.UserLoginRequest;
import com.snwolf.swtutu.model.dto.user.UserQueryRequest;
import com.snwolf.swtutu.model.dto.user.UserRegisterRequest;
import com.snwolf.swtutu.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snwolf.swtutu.model.vo.LoginUserVO;
import com.snwolf.swtutu.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author zhang
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-12-09 20:03:27
*/
public interface UserService extends IService<User> {

    long userRegister(UserRegisterRequest userRegisterRequest);

    String getEncryptPassword(String userPassword);

    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 获得脱敏后的登录用户信息
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 系统内部用的获取用户的接口
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户退出登录
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获得脱敏后的用户信息
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获得脱敏后的用户信息列表
     * @param userList 用户列表
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
