package com.snwolf.swtutu.aop;

import com.snwolf.swtutu.annotation.AuthCheck;
import com.snwolf.swtutu.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/9/2024
 * @Description: 用户权限校验切面
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 拦截需要权限校验的方法
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint point, AuthCheck authCheck) throws Throwable {
        // 获取当前登录用户

        // 检查是否有mustRole限制

        // 检查用户角色和mustRole是否匹配


    }
}
