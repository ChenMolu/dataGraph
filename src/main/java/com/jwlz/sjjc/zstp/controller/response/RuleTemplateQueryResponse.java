package com.jwlz.sjjc.zstp.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jwlz.sjjc.zstp.entity.vo.RuleTemplateVO;
import lombok.Data;

import java.util.List;

/**
 * @author luhuachen
 */
@Data
public class RuleTemplateQueryResponse {

    @JsonProperty("num")
    private int num;

    @JsonProperty("Knowledges")
    private List<RuleTemplateVO> knowledges;
}
