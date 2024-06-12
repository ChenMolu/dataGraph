package com.jwlz.sjjc.zstp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author luhuachen
 */
@Data
@TableName(value = "SJJC_SYS.T_JWLZ_SJJC_ZSPG_GZMB")
public class RuleTemplateDO {

    @TableId(value = "GZID")
    private String GZID;

    @TableField("GZLX")
    private int GZLX;

    @TableField("GZMB")
    private String GZMB;

    @TableField("GZMS")
    private String GZMS;

}
