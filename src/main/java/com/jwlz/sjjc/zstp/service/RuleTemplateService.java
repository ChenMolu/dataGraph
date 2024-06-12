package com.jwlz.sjjc.zstp.service;

import com.jwlz.sjjc.zstp.controller.request.RuleInputRequest;
import com.jwlz.sjjc.zstp.controller.request.RuleTemplateInputRequest;
import com.jwlz.sjjc.zstp.controller.request.RuleTemplateUpdateRequest;
import com.jwlz.sjjc.zstp.controller.request.RuleUpdateRequest;
import com.jwlz.sjjc.zstp.entity.vo.RuleTemplateVO;
import com.jwlz.sjjc.zstp.entity.vo.ValidationRuleVO;

import java.util.List;

/**
 * @author luhuachen
 */
public interface RuleTemplateService {

    public List<RuleTemplateVO> queryList(String ruleType);

    public int modify(RuleTemplateUpdateRequest request);

    public int add(RuleTemplateInputRequest request);

    public int delete(String ruleID);
}
