package com.snwolf.swtutu.model.dto.picture;

import lombok.Data;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/11/2024
 * @Description: 批量导入图片请求参数
 */
@Data
public class PictureUploadByBatchRequest {

    private String searchText;

    /**
     * 默认抓取10条
     */
    private Integer count = 10;

    /**
     * 图片名称前缀
     */
    private String namePrefix = searchText;
}
