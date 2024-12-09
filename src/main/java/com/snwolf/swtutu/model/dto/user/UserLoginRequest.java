package com.snwolf.swtutu.model.dto.user;

import lombok.Data;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/9/2024
 * @Description: 用户登录请求
 */
@Data
public class UserLoginRequest {
    private String userAccount;
    private String userPassword;
}
