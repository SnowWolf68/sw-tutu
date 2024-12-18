package com.snwolf.swtutu.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.snwolf.swtutu.model.entity.User;
import lombok.Data;

import java.util.Date;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/9/2024
 * @Description: 用户视角脱敏用户信息
 */
@Data
public class UserVO {
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    public static UserVO objToVO(User user){
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    public static User VOToObj(UserVO userVO){
        User user = new User();
        BeanUtil.copyProperties(userVO, user);
        return user;
    }
}
