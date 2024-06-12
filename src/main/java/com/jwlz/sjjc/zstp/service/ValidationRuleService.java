package com.jwlz.sjjc.zstp.service;

import com.jwlz.sjjc.zstp.controller.request.RuleInputRequest;
import com.jwlz.sjjc.zstp.controller.request.RuleUpdateRequest;
import com.jwlz.sjjc.zstp.entity.vo.ValidationRuleVO;

import java.util.List;

/**
 * @author luhuachen
 */
public interface ValidationRuleService {

    public List<ValidationRuleVO> queryList(String ruleType);

    public int modify(RuleUpdateRequest request);

    public int add(RuleInputRequest request);

    public int delete(String ruleID);

}
