package com.jwlz.sjjc.zstp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jwlz.sjjc.zstp.controller.request.RuleTemplateInputRequest;
import com.jwlz.sjjc.zstp.controller.request.RuleTemplateUpdateRequest;
import com.jwlz.sjjc.zstp.entity.RuleTemplateDO;
import com.jwlz.sjjc.zstp.entity.vo.RuleTemplateVO;
import com.jwlz.sjjc.zstp.mapper.RuleTemplateMapper;
import com.jwlz.sjjc.zstp.service.RuleTemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author luhuachen
 */
@Service
@Slf4j
public class RuleTemplateServiceImpl implements RuleTemplateService {

    @Resource
    private RuleTemplateMapper ruleTemplateMapper;

    @Override
    public List<RuleTemplateVO> queryList(String ruleType) {
        QueryWrapper<RuleTemplateDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("GZLX", ruleType);
        List<RuleTemplateDO> ruleTemplateDOS = ruleTemplateMapper.selectList(queryWrapper);
        ArrayList<RuleTemplateVO> ruleTemplateVOS = new ArrayList<>();
        for(RuleTemplateDO templateDO:ruleTemplateDOS) {
            RuleTemplateVO ruleTemplateVO = new RuleTemplateVO();
            ruleTemplateVO.setGZID(templateDO.getGZID());
            ruleTemplateVO.setGZMB(templateDO.getGZMB());
            ruleTemplateVO.setGZMS(templateDO.getGZMS());
            ruleTemplateVO.setGZLX(templateDO.getGZLX());
            ruleTemplateVOS.add(ruleTemplateVO);
        }
        return ruleTemplateVOS;
    }

    @Override
    public int modify(RuleTemplateUpdateRequest request) {
        RuleTemplateDO ruleTemplateDO = new RuleTemplateDO();
        ruleTemplateDO.setGZID(request.getGZID());
        ruleTemplateDO.setGZMS(request.getGZMS());
        ruleTemplateDO.setGZLX(request.getGZLX());
        ruleTemplateDO.setGZMB(request.getGZMB());
        int update = ruleTemplateMapper.updateById(ruleTemplateDO);
        return update;
    }

    @Override
    public int add(RuleTemplateInputRequest request) {
        RuleTemplateDO ruleTemplateDO = new RuleTemplateDO();
        ruleTemplateDO.setGZID(generateID());
        ruleTemplateDO.setGZMS(request.getGZMS());
        ruleTemplateDO.setGZLX(request.getGZLX());
        ruleTemplateDO.setGZMB(request.getGZMB());
        int insert = ruleTemplateMapper.insert(ruleTemplateDO);
        return insert;
    }

    @Override
    public int delete(String ruleID) {
        int delete = ruleTemplateMapper.deleteById(ruleID);
        return delete;
    }

    private String generateID() {
        // 生成UUID并移除连字符
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 截取前30个字符
        return uuid.substring(0, 30);
    }
}
