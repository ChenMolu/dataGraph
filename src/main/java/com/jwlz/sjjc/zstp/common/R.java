package com.jwlz.sjjc.zstp.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 通用返回结果类
 *
 * @param <T>
 * @author luhuachen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class R<T> {

    private String status;

    private String message;

    private String time;

    private T data; //数据

//    private Map map = new HashMap(); //动态数据

    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.status = "200";
        r.time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        return r;
    }

    public static <T> R<T> success(T object, String message) {
        R<T> r = new R<T>();
        r.data = object;
        r.status = "200";
        r.message = message;
        r.time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.message = msg;
        r.status = "Ax000000003";
        r.time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        return r;
    }

//    public R<T> add(String key, Object value) {
//        this.map.put(key, value);
//        return this;
//    }

}
