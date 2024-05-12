package com.jwlz.sjjc.zstp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@TableName(value = "SYSDBA.D_RELATIONSHIP")
public class Relation {
//    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    private int entityId;

    private String subject;

    private String objectItem;

    private String relation;

    private int isDeleted;

    private int objectId;

}
