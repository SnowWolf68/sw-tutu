package com.snwolf.swtutu.model.dto.file;

import lombok.Data;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/11/2024
 * @Description: 图片上传返回结果
 */
@Data
public class UploadFileResult {

    private String url;

    private String name;

    private String introduction;

    private String category;

    private String tags;

    private Long picSize;

    private Integer picWidth;

    private Integer picHeight;

    private Double picScale;

    private String picFormat;
}
