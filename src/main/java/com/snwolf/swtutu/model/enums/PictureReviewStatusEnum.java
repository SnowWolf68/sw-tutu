package com.snwolf.swtutu.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/9/2024
 * @Description: 用户角色枚举
 */
@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static PictureReviewStatusEnum getEnumByValue(int value) {
        if(ObjUtil.isEmpty(value)){
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (value == pictureReviewStatusEnum.value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
