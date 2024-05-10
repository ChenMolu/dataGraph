package com.hnu.datagraph.entity.dto;

import lombok.Data;

/**
 * 联系
 */
@Data
public class Link {
    //源id
    private String source;

    //目的id
    private String target;

    //联系名称（可以为空）
    private String value;
}
