package com.hnu.datagraph.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("返回结果")
public class R<T> {

    @ApiModelProperty("状态码")
    private Integer statusCode;

    @ApiModelProperty("返回信息")
    private String msg;

    @ApiModelProperty("返回数据")
    private T data;

    public static <T> R<T> success(String msg) {
        R<T> result = new R<>();
        result.statusCode = 200; // Use literal value for OK status code
        result.msg = msg;
        return result;
    }

    public static <T> R<T> success() {
        R<T> result = new R<>();
        result.statusCode = 200; // Use literal value for OK status code
        result.msg = "请求成功";
        return result;
    }

    public static <T> R<T> error() {
        R<T> result = new R<>();
        result.statusCode = 500; // Use literal value for internal server error
        result.msg = "执行异常";
        return result;
    }

    public static <T> R<T> error(String msg) {
        R<T> result = new R<>();
        result.statusCode = 500; // Use literal value for internal server error
        result.msg = "执行异常:" + msg;
        return result;
    }

    public static <T> R<T> error(Integer statusCode, String msg) {
        R<T> result = new R<>();
        result.statusCode = statusCode;
        result.msg = msg;
        return result;
    }

    public boolean isSuccess() {
        return this.statusCode == 200; // Use literal value for OK status code
    }


    public R<T> data(T data) {
        this.data = data;
        return this;
    }
}
