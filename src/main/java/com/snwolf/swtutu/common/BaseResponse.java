package com.snwolf.swtutu.common;

import com.snwolf.swtutu.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/9/2024
 * @Description:
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;

    private String message;

    private T data;

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, T data){
        this(code, "", data);
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
