package com.jwlz.sjjc.zstp.controller;


import com.alibaba.fastjson2.JSONObject;
import com.jwlz.sjjc.zstp.common.R;
import com.jwlz.sjjc.zstp.controller.request.RuleInputRequest;
import com.jwlz.sjjc.zstp.controller.request.RuleUpdateRequest;
import com.jwlz.sjjc.zstp.controller.response.RuleQueryResponse;
import com.jwlz.sjjc.zstp.entity.vo.ValidationRuleVO;
import com.jwlz.sjjc.zstp.service.ValidationRuleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author luhuachen
 */
@Slf4j
@RestController
@RequestMapping("/zsjg/zspg")
public class ValidationRuleController {

    @Resource
    private ValidationRuleService validationRuleService;

    private static final int TRUE = 1;
    private static final int FALSE = 1;

    @PostMapping("/ruleInput")
    public R ruleInput(@RequestBody RuleInputRequest request) {
        if (validationRuleService.add(request) == TRUE) {
            return R.success(new JSONObject(), "插入成功");
        } else {
            return R.error("没有权限使用相应的服务");
        }
    }

    @GetMapping("/ruleList")
    public R<RuleQueryResponse> getRuleList(@RequestParam("ruleType") String ruleType) {
        List<ValidationRuleVO> validationRuleVOS = validationRuleService.queryList(ruleType);
        RuleQueryResponse ruleQueryResponse = new RuleQueryResponse();
        ruleQueryResponse.setNum(validationRuleVOS.size());
        ruleQueryResponse.setKnowledges(validationRuleVOS);
        return R.success(ruleQueryResponse, "查询成功");
    }

    @PutMapping("/ruleUpdate")
    public R updateRule(@RequestBody RuleUpdateRequest request) {
        if (validationRuleService.modify(request) == TRUE) {
            return R.success(new JSONObject(), "修改成功");
        } else {
            return R.error("没有权限使用相应的服务");
        }
    }

    @DeleteMapping("/ruleDelete")
    public R deleteRule(@RequestParam("ruleID") String ruleID) {
        if(validationRuleService.delete(ruleID)==TRUE) {
            return R.success(new JSONObject(), "删除成功");
        } else {
            return R.error("没有权限使用相应的服务");
        }
    }
}
