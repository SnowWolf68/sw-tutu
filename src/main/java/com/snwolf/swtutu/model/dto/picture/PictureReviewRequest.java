package com.snwolf.swtutu.model.dto.picture;

import lombok.Data;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/14/2024
 * @Description: 图片审核请求
 */
@Data
public class PictureReviewRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人id
     */
    private Long reviewerId;
}
