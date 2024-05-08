package com.hnu.datagraph.controller;


import com.hnu.datagraph.entity.GstoreResult;
import com.hnu.datagraph.service.GstoreService;
import com.hnu.datagraph.entity.vo.R;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

@Api(tags = "gstore相关接口")
@RestController
@RequestMapping("/gstore")
@Slf4j
public class GstoreController {

    @Autowired
    private GstoreService gstoreService;

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
            if (res.getAnsNum() == 0) {
                R<GstoreResult> r = new R<>(1001, "未查询到结果", res);
                return r;
            }
            return R.<GstoreResult>success("查询成功").data(res);
        } else {
            return R.error("查询失败");
        }
    }
}

