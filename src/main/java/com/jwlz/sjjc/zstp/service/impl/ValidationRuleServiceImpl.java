package com.jwlz.sjjc.zstp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jwlz.sjjc.zstp.controller.request.RuleInputRequest;
import com.jwlz.sjjc.zstp.controller.request.RuleUpdateRequest;
import com.jwlz.sjjc.zstp.entity.ValidationRuleDO;
import com.jwlz.sjjc.zstp.entity.vo.ValidationRuleVO;
import com.jwlz.sjjc.zstp.mapper.ValidationRuleMapper;
import com.jwlz.sjjc.zstp.service.ValidationRuleService;
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
public class ValidationRuleServiceImpl implements ValidationRuleService {

    @Resource
    private ValidationRuleMapper validationRuleMapper;

    @Override
    public List<ValidationRuleVO> queryList(String ruleType) {
        QueryWrapper<ValidationRuleDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("GZLX", ruleType);
        List<ValidationRuleDO> validationRuleDOS = validationRuleMapper.selectList(queryWrapper);
        ArrayList<ValidationRuleVO> validationRuleVOS = new ArrayList<>();
        for(ValidationRuleDO validationRuleDO : validationRuleDOS) {
            ValidationRuleVO validationRuleVO = new ValidationRuleVO();
            validationRuleVO.setID(validationRuleDO.getID());
            validationRuleVO.setCZSJ(validationRuleDO.getCZSJ());
            validationRuleVO.setGZMS(validationRuleDO.getGZMS());
            validationRuleVO.setGZLX(validationRuleDO.getGZLX());
            validationRuleVO.setGZ(validationRuleDO.getGZ());
            validationRuleVOS.add(validationRuleVO);
        }
        return validationRuleVOS;
    }

    @Override
    public int modify(RuleUpdateRequest request) {
        ValidationRuleDO validationRuleDO = new ValidationRuleDO();
        validationRuleDO.setID(request.getID());
        validationRuleDO.setCZSJ(request.getCZSJ());
        validationRuleDO.setGZMS(request.getGZMS());
        validationRuleDO.setGZLX(request.getGZLX());
        validationRuleDO.setGZ(request.getGZ());
        int update = validationRuleMapper.updateById(validationRuleDO);
        return update;
    }

    @Override
    public int add(RuleInputRequest request) {
        ValidationRuleDO validationRuleDO = new ValidationRuleDO();
        validationRuleDO.setID(generateID());
        validationRuleDO.setCZSJ(request.getCZSJ());
        validationRuleDO.setGZMS(request.getGZMS());
        validationRuleDO.setGZLX(request.getGZLX());
        validationRuleDO.setGZ(request.getGZ());
        int insert = validationRuleMapper.insert(validationRuleDO);
        return insert;
    }

    @Override
    public int delete(String ruleID) {
        int delete = validationRuleMapper.deleteById(ruleID);
        return delete;
    }

    private String generateID() {
        // 生成UUID并移除连字符
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 截取前30个字符
        return uuid.substring(0, 30);
    }
}
