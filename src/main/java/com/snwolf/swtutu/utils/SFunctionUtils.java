package com.snwolf.swtutu.utils;

import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Map;

import static java.lang.invoke.LambdaMetafactory.FLAG_SERIALIZABLE;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/14/2024
 * @Description: 获取字段对应的SFunction对象
 * 工具类参考链接: https://blog.csdn.net/m0_59084856/article/details/138452605
 */
public class SFunctionUtils {

    private static final Map<String, SFunction> functionMap = new java.util.HashMap<>();

    /**
     * 获取与实体类字段对应的 SFunction 对象。
     * @param entityClass 实体类的 Class 对象。
     * @param fieldName 实体类中的字段名。
     * @return 返回找到的 SFunction 对象。
     */
    public static SFunction getSFunction(Class<?> entityClass, String fieldName) {
        // 检查缓存中是否已经有了对应的 SFunction 对象。
        if (functionMap.containsKey(entityClass.getName() + fieldName)) {
            return functionMap.get(entityClass.getName() + fieldName);
        }
        // 获取实体类中名为 fieldName 的字段。
        Field field = getDeclaredField(entityClass, fieldName);
        if (field == null) {
//            //如果字段不存在，使用 ExceptionUtils 抛出一个异常，指出实体类中没有找到该字段。
//            throw ExceptionUtils.mpe("This class %s is not have field %s ", entityClass.getName(), fieldName);
            return null;
        }
        SFunction func = null;
        // 获取 MethodHandles.Lookup 实例，用于反射操作。
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        // 定义方法类型，表示实体类的实例方法，该方法返回字段的类型。
        MethodType methodType = MethodType.methodType(field.getType(), entityClass);
        // 用于存储 LambdaMetafactory 创建的 CallSite 对象。
        final CallSite site;
        // 构造标准的 Java getter 方法名。
        String getFunName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            // 使用 LambdaMetafactory 创建一个动态的 SFunction 实例。
            site = LambdaMetafactory.altMetafactory(
                    lookup,
                    "invoke",
                    MethodType.methodType(SFunction.class),
                    methodType,
                    lookup.findVirtual(entityClass, getFunName, MethodType.methodType(field.getType())),
                    methodType,
                    FLAG_SERIALIZABLE
            );
            // 使用 CallSite 来获取 SFunction 实例。
            func = (SFunction) site.getTarget().invokeExact();
            // 将生成的 SFunction 实例存储到缓存中。
            functionMap.put(entityClass.getName() + field.getName(), func);
            return func;
        } catch (Throwable e) {
            // 如果在创建 SFunction 过程中发生异常，抛出异常，指出实体类中没有找到对应的 getter 方法。
            throw ExceptionUtils.mpe("This class %s is not have method %s ", entityClass.getName(), getFunName);
        }
    }

    /**
     * 递归获取类中声明的字段，包括私有字段。
     * @param clazz 要检查的类。
     * @param fieldName 要查找的字段名。
     * @return 返回找到的 Field 对象，如果没有找到则返回 null。
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        Field field = null;
        // 遍历类及其父类，直到到达 Object 类。
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                // 尝试获取声明的字段。
                field = clazz.getDeclaredField(fieldName);
                // 如果找到字段，返回该字段。
                return field;
            } catch (NoSuchFieldException e) {
                // 如果没有找到字段，继续查找父类。
                // 这里不处理异常，让其继续执行循环。
            }
        }
        // 如果没有找到字段，返回 null。
        return null;
    }

}
