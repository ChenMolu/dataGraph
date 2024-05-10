package com.hnu.datagraph.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "SYSDBA.D_ENTITY")
public class Item {
    private int id;
    private String name;
    private String description;
    private String type;
}
