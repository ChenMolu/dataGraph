package com.hnu.datagraph.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hnu.datagraph.common.R;
import com.hnu.datagraph.entity.Graph;
import com.hnu.datagraph.entity.Relation;
import com.hnu.datagraph.entity.vo.Answer;
import com.hnu.datagraph.mapper.RelationMapper;
import com.hnu.datagraph.service.NlpService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/nlp")
public class QAController {

    @Resource
    private NlpService nlpService;

    @Resource
    private RelationMapper relationMapper;

    @GetMapping(value = "/getAnswer", produces = "application/json;charset=UTF-8")
    public R<Answer> getAnswer(@RequestParam String question) {
        log.info("question:{}", question);
        Answer answer = new Answer();
        List<String> structuredQueryList = nlpService.getStructuredQueryList(question);
        log.info("structuredQueryList:{}", structuredQueryList.toString());
        if (structuredQueryList.size() <= 1) {
            return R.error("抱歉，未查询到相关信息！");
        } else {
            String entity = structuredQueryList.get(0);
            QueryWrapper<Relation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("SUBJECT", entity);
            List<Relation> relations = relationMapper.selectList(queryWrapper);
            if (relations.isEmpty()) {
                return R.error("抱歉，未查询到相关信息！");
            }
            log.info(relations.toString());
            Map<String, String> res = new HashMap<>(16);
            Graph graph = new Graph();
            graph.setNode(entity);
            Map<String, String> map = new HashMap<>(16);
            for (Relation relation : relations) {
                map.put(relation.getRelation(), relation.getObjectItem());
            }
            graph.setRelation(map);
            for (int i = 1; i < structuredQueryList.size(); i++) {

                for (Relation relation : relations) {
                    if (relation.getRelation().equals(structuredQueryList.get(i))) {
                        res.put(structuredQueryList.get(i), relation.getObjectItem());
                    }
                }
            }
            answer.setAnswer(res);
            answer.setGraph(graph);
        }
        return R.success(answer);
    }

}
