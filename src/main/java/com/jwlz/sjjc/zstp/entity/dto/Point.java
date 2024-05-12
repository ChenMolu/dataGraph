package com.jwlz.sjjc.zstp.entity.dto;

import lombok.Data;

/**
 * 实体
 */
@Data
public class Point {
    //名称
    private String name;

    //编号
    private long id;

    //节点大小
    private int symbolSize;

    //地区分布
    private String category;
}
