package com.jwlz.sjjc.zstp.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistics {
    // 三元组
    private long tripleAmount;
    // 主语数量
    private long subjectAmount;
    // 机构
    private long institutionAmount;
    // 人员
    private long personnelAmount;
    // 装备
    private long equipmentAmount;
    // 物资
    private long materialAmount;
    // 设施
    private long facilityAmount;
    // 环境
    private long envAmount;
    // 事件
    private long eventAmount;
    // 行动
    private long actionAmount;

}
