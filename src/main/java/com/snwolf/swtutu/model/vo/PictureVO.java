package com.snwolf.swtutu.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.snwolf.swtutu.model.entity.Picture;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/11/2024
 * @Description: 图片上传成功返回对象
 */
@Data
public class PictureVO {

    private Long id;

    private String url;

    private String name;

    private String introduction;

    private String category;

    private List<String> tags;

    private Long picSize;

    private Integer picWidth;

    private Integer picHeight;

    private Double picScale;

    private String picFormat;

    private UserVO user;

    private Date createTime;

    private Date editTime;

    private Date updateTime;

    public static PictureVO ObjToVO(Picture picture){
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(picture, pictureVO);
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }

    public static Picture VOToObj(PictureVO pictureVO){
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVO, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }
}
