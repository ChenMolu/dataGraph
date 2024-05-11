package com.hnu.datagraph.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hnu.datagraph.common.R;
import com.hnu.datagraph.entity.GstoreResult;
import com.hnu.datagraph.entity.Relation;
import com.hnu.datagraph.entity.vo.Point;
import com.hnu.datagraph.mapper.RelationMapper;
import com.hnu.datagraph.service.GstoreService;
import com.hnu.datagraph.service.NlpService;
import io.swagger.annotations.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api(tags = "gstore相关接口")
@RestController
@RequestMapping("/gstore")
@Slf4j
public class GstoreController {

    @Autowired
    private GstoreService gstoreService;

    @Resource
    private NlpService nlpService;

    @Resource
    private RelationMapper relationMapper;

    @GetMapping(value = "/getEntity", produces = "application/json;charset=UTF-8")
    public R<List<Point>> getEntity(@RequestParam String name) {
        List<String> structuredQueryList = nlpService.getStructuredQueryList(name);
        log.info("structuredQueryList:{}", structuredQueryList.toString());
        List<Point> points = new ArrayList<>();
        if (structuredQueryList.size() <= 0) {
            return R.error("抱歉，未查询到相关信息！");
        } else {
            String entity = structuredQueryList.get(0);
            QueryWrapper<Relation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("SUBJECT", entity);
            List<Relation> relations = relationMapper.selectList(queryWrapper);
            if (relations.isEmpty()) {
                return R.error("抱歉，未查询到相关信息！");
            }

            points.add(new Point(0, entity, "", -1));
            for (int i = 1; i <= relations.size(); i++) {
                Point point = new Point(i, relations.get(i - 1).getObjectItem(), relations.get(i - 1).getRelation(), 0);
                points.add(point);
            }
        }
        return R.success(points);
    }

    /**
     * sparql查询接口
     */
    @GetMapping
    @ResponseBody
    @ApiOperation(value = "sparql查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "database", value = "要查询的数据库名称",
                    required = true, dataType = "string", example = "KQAPro"),
            @ApiImplicitParam(paramType = "body", name = "sparql",
                    value = "sparql语句", required = true, dataType = "string",
                    example = "SELECT DISTINCT ?e " +
                            "WHERE { ?e <pred:instance_of> ?c . ?c <pred:name> \\\"town\\\" . " +
                            "?e <TOID> ?pv . ?pv <pred:value> \\\"4000000074573917\\\" . " +
                            "?e <OS_grid_reference> ?pv_1 . ?pv_1 <pred:value> \\\"SP8778\\\" .  }")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功, 查询结果在data里面, 格式为json"),
    })
    public R<GstoreResult> query(@ApiIgnore @RequestBody Map map) {
        String database = (String) map.getOrDefault("database", "KQAPro");
        String sparQL = (String) map.getOrDefault("sparql", "select * where {?s ?p ?o} limit 10");
        log.info("此次查询: database: {}, sparql: {}", database, sparQL);

        GstoreResult res = gstoreService.query(database, sparQL);
        log.info("此次查询结果: {}", res);

        if (res == null) {
            return R.error("数据库连接错误");
        }
        if (res.getStatusCode() == 0) {
//            if (res.getAnsNum() == 0) {
////                R<GstoreResult> r = new R<>(1001, "未查询到结果", res);
////                return r;
//            }
//            return R.<GstoreResult>success("查询成功").data(res);
            return R.success(res);
        } else {
            return R.error("查询失败");
        }
    }
}

