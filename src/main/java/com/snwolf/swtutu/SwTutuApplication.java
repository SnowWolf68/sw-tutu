package com.snwolf.swtutu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.snwolf.swtutu.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)     // 开启aop, 能够使用AopContext.currentProxy()得到当前的代理对象
public class SwTutuApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwTutuApplication.class, args);
    }

}
