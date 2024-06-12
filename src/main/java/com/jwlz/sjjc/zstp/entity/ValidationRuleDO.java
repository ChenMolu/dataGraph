package com.jwlz.sjjc.zstp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "SJJC_SYS.T_JWLZ_SJJC_ZSPG_PGGZ")
public class ValidationRuleDO {

    @TableId(value = "ID")
    private String ID;

    @JsonProperty("GZLX")
    @TableField("GZLX")
    private int GZLX;

    @JsonProperty("GZ")
    @TableField("GZ")
    private String GZ;

    @JsonProperty("GZMS")
    @TableField("GZMS")
    private String GZMS;

    @JsonProperty("CJSJ")
    @TableField("CJSJ")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date CZSJ;
}
