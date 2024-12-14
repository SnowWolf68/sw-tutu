package com.snwolf.swtutu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snwolf.swtutu.model.entity.Picture;
import com.snwolf.swtutu.service.PictureService;
import com.snwolf.swtutu.utils.SFunctionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/14/2024
 * @Description:
 */
@SpringBootTest
public class TestSFunction {

    @Resource
    private PictureService pictureService;

    @Test
    public void test(){
        LambdaQueryWrapper<Picture> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper queryWrapper = lambdaQueryWrapper.eq(SFunctionUtils.getSFunction(Picture.class, "id"), "1867174524095401985");
        Picture one = pictureService.getOne(queryWrapper);
        System.out.println(one.toString());
    }
}
