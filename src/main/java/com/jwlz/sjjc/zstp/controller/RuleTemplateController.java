package com.jwlz.sjjc.zstp.controller;

import com.alibaba.fastjson2.JSONObject;
import com.jwlz.sjjc.zstp.common.R;
import com.jwlz.sjjc.zstp.controller.request.RuleTemplateInputRequest;
import com.jwlz.sjjc.zstp.controller.request.RuleTemplateUpdateRequest;
import com.jwlz.sjjc.zstp.controller.response.RuleTemplateQueryResponse;
import com.jwlz.sjjc.zstp.entity.vo.RuleTemplateVO;
import com.jwlz.sjjc.zstp.service.RuleTemplateService;
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
public class RuleTemplateController {

    @Resource
    private RuleTemplateService ruleTemplateService;

    private static final int TRUE = 1;
    private static final int FALSE = 1;

    @PostMapping("/ruleTemplateInput")
    public R ruleTemplateInput(@RequestBody RuleTemplateInputRequest request) {
        if (ruleTemplateService.add(request) == TRUE) {
            return R.success(new JSONObject(), "插入成功");
        } else {
            return R.error("没有权限使用相应的服务");
        }
    }

    @GetMapping("/ruleTemplateList")
    public R<RuleTemplateQueryResponse> getRuleTemplateList(@RequestParam("ruleType") String ruleType) {
        List<RuleTemplateVO> ruleTemplateVOS = ruleTemplateService.queryList(ruleType);
        RuleTemplateQueryResponse ruleTemplateQueryResponse = new RuleTemplateQueryResponse();
        ruleTemplateQueryResponse.setNum(ruleTemplateVOS.size());
        ruleTemplateQueryResponse.setKnowledges(ruleTemplateVOS);
        return R.success(ruleTemplateQueryResponse, "查询成功");
    }

    @PutMapping("/ruleTemplateUpdate")
    public R updateRuleTemplate(@RequestBody RuleTemplateUpdateRequest request) {
        if (ruleTemplateService.modify(request) == TRUE) {
            return R.success(new JSONObject(), "修改成功");
        } else {
            return R.error("没有权限使用相应的服务");
        }
    }

    @DeleteMapping("/ruleTemplateDelete")
    public R deleteRuleTemplate(@RequestParam("ruleID") String ruleID) {
        if (ruleTemplateService.delete(ruleID) == TRUE) {
            return R.success(new JSONObject(), "删除成功");
        } else {
            return R.error("没有权限使用相应的服务");
        }
    }


}
