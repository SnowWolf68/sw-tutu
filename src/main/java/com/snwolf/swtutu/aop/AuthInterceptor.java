package com.snwolf.swtutu.aop;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.snwolf.swtutu.annotation.AuthCheck;
import com.snwolf.swtutu.exception.ErrorCode;
import com.snwolf.swtutu.exception.ThrowUtils;
import com.snwolf.swtutu.model.entity.User;
import com.snwolf.swtutu.model.enums.UserRoleEnum;
import com.snwolf.swtutu.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = attributes.getRequest();
        User loginUser = userService.getLoginUser(request);
        // 检查是否有mustRole限制
        String mustRole = authCheck.mustRole();
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 检查用户角色和mustRole是否匹配
        // 1. 如果mustRole为空, 默认需要用户登录
        if (StrUtil.isBlank(mustRole)){
            ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCode.NO_AUTH_ERROR, "未登录");
            return point.proceed();
        }
        // 2. 如果mustRole不为空, 则需要查看用户是否有mustRole中的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        ThrowUtils.throwIf(ObjectUtil.isNull(userRoleEnum) || !mustRoleEnum.equals(userRoleEnum), ErrorCode.NO_AUTH_ERROR, "未授权");
        return point.proceed();
    }
}
