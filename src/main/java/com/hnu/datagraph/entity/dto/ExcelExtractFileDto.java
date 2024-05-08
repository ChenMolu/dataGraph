package com.hnu.datagraph.entity.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class ExcelExtractFileDto {
    // 主语
    private String subject;
    // 谓语
    private String predicate;
    // 宾语
    private String object;
    // 类型
    private String type;
}
