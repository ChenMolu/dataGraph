package com.jwlz.sjjc.zstp.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jwlz.sjjc.zstp.common.Constant;
import com.jwlz.sjjc.zstp.common.R;
import com.jwlz.sjjc.zstp.entity.GstoreQueryDataRequest;
import com.jwlz.sjjc.zstp.entity.GstoreResult;
import com.jwlz.sjjc.zstp.entity.Relation;
import com.jwlz.sjjc.zstp.entity.vo.Point;
import com.jwlz.sjjc.zstp.exception.BadRequestException;
import com.jwlz.sjjc.zstp.mapper.RelationMapper;
import com.jwlz.sjjc.zstp.service.GstoreService;
import com.jwlz.sjjc.zstp.service.NlpService;
import com.jwlz.sjjc.zstp.utils.GStoreConnector;
import com.jwlz.sjjc.zstp.utils.KgUtils;
import com.jwlz.sjjc.zstp.utils.StringUtil;
import io.swagger.annotations.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

@Api(tags = "gstore相关接口")
@RestController
@RequestMapping("/zstp/gstore")
@Slf4j
public class GstoreController {

    @Autowired
    private GstoreService gstoreService;

    @Resource
    private NlpService nlpService;

    @Resource
    private RelationMapper relationMapper;

    @Value("${seniorFun}")
    private String seniorFunStr;

    @Value("${nodeNum}")
    private Integer nodeNum;

    @PostMapping(value = "/querydata")
    public R<JSONObject> querydata(@RequestBody GstoreQueryDataRequest request) {
        log.info(request.toString());
        String ip = "61.136.101.220", port = "20046", version = "1.3", gstore_username = "root", gstore_password = "123456", accessType = "ghttp";
        GStoreConnector gc = GStoreConnector.getGStoreConnector(version, ip, Integer.valueOf(port), gstore_username, gstore_password, accessType);
        String sparql = request.getSparql();
        String dbname = request.getDbname();
        String limit = request.getLimit();
        String type = request.getType();
        boolean isStandard = true;
        // 普通查询
        if (StringUtils.isEmpty(limit)) {
            limit = "100";
        }
        try {
            sparql = URLDecoder.decode(sparql, "UTF-8");
            gc.load(dbname);
            long starttime = System.currentTimeMillis();
            JSONObject resultJson = new JSONObject();

            try {
                sparql = URLDecoder.decode(sparql, "UTF-8").trim();
            } catch (UnsupportedEncodingException e) {
                return R.error("sparql编码异常");
            }

            if ("2".equals(type) && (sparql.toLowerCase().startsWith("insert") || sparql.toLowerCase().startsWith("delete"))) {
                if ("0.8".equals(gc.getVersion())) {
                    return R.error("当前版本没有此功能");
                }
                // 事务操作
                try {
                    JSONObject gcResult = JSONObject.parseObject(gc.begin(dbname));
                    if (gcResult.getInteger("StatusCode") == 0) {
                        String tid = gcResult.getString("TID");
                        gcResult = JSONObject.parseObject(gc.tquery(dbname, sparql, tid));
                        long costtime = System.currentTimeMillis() - starttime;
                        Integer statusCode = gcResult.getInteger("StatusCode");
                        if (statusCode == 0) {
                            resultJson.put("costtime", costtime);
                            return R.success(resultJson);
                        } else {
                            if (statusCode == 1005) {
                                return R.error("语法错误");
                            }
                            return R.error("操作异常：" + gcResult.getString("StatusMsg"));
                        }
                    } else {
                        return R.error("操作异常：" + gcResult.getString("StatusMsg"));
                    }
                } catch (Exception e) {
                    return R.error("操作异常：" + e.getMessage());
                }
            } else {
                String[] seniorFun = seniorFunStr.split(",");
                String n = StrUtil.getContainsStrIgnoreCase(sparql.replace(" ", ""), seniorFun);
                if (StringUtil.isNotBlank(n) && n.length() > 0) {
//                return new ResultInfo(Constant.FAIL, "普通查询窗口不支持高级函数查询：" + n);
                } else if (StringUtils.isEmpty(limit)) {
                    // 普通查询
                    limit = "100";
                }
                try {
                    if (sparql.toLowerCase().startsWith("prefix")) {
                        Query query = QueryFactory.create(sparql);
                        if (query.isSelectType() && !sparql.toLowerCase().contains("limit")) {
                            sparql = sparql + " limit " + limit;

                        }
//                    else if (limitflag.equalsIgnoreCase("true")) {
//                        String limitnumnew = sparql.substring(sparql.indexOf("limit") + 5, sparql.length());
//                        System.err.println(limitnumnew);
//                        if (Long.parseLong(limitnumnew.trim()) > Long.parseLong(limitnum)) {
//                            throw new BadRequestException("超出查询数量限制：" + limitnum);
//                        }
//                    }
                    } else if (sparql.toLowerCase().startsWith("select") && !sparql.toLowerCase().contains("limit")) {
                        sparql = sparql + " limit " + limit;
                    }
//                else if (limitflag.equalsIgnoreCase("true")) {
//                    String limitnumnew = sparql.substring(sparql.indexOf("limit") + 5, sparql.length());
//                    if (Long.parseLong(limitnumnew.trim()) > Long.parseLong(limitnum)) {
//                        throw new BadRequestException("超出查询数量限制：" + limitnum);
//                    }
//                }

                    String result = gc.query(dbname, sparql);
                    JSONObject gStoreJson = JSONObject.parseObject(result);
                    long costtime = System.currentTimeMillis() - starttime;
                    Integer statusCode = gStoreJson.getInteger("StatusCode");
                    if (statusCode == 0) {
                        resultJson.put("costtime", costtime);
                        if (sparql.toLowerCase().startsWith("insert") || sparql.toLowerCase().startsWith("delete")) {
                            // 存入磁盘
                            gc.checkpoint(dbname);
                            return R.success(resultJson);
                        }
                        if (sparql.toLowerCase().startsWith("prefix")) {
                            Query query = QueryFactory.create(sparql);
//                            if (query.isSelectType()) {
//                                String filePath = request.getServletContext().getRealPath("/") + "resultfile";
//                                String filename = saveJSONFile(gStoreJson, filePath);
//                                Map iconMap = new HashMap();
//                                if (isStandard) {
//                                    iconMap = KgUtils.getProjectEntityIcons(gc, dbname);
//                                }
//                                resultJson = KgUtils.genResultJson(sparql, gStoreJson, iconMap, isStandard, nodeNum);
//                                resultJson.put("file", filename);
//                            }
                            if (query.isAskType()) {
                                String label = null;
                                if ("0.8".equals(gc.getVersion())) {
                                    label = gStoreJson.getJSONObject("results").getJSONArray("bindings").
                                            getJSONObject(0).getJSONObject("askResult").getBoolean("value") == false ? "false" : "true";
                                } else {
                                    label = gStoreJson.getJSONObject("results").getJSONArray("bindings").
                                            getJSONObject(0).getJSONObject("_askResult").getString("value");
                                }
                                return R.success(JSONObject.parseObject(label));
                            }
                        } else {
                            if (sparql.toLowerCase().startsWith("select")) {
                                String sparqlNew = sparql.replace(" ", "").toLowerCase();
                                if (sparqlNew.contains("count(") || sparqlNew.contains("sum(")
                                        || sparqlNew.contains("avg(") || sparqlNew.contains("min(")
                                        || sparqlNew.contains("max(")) {

                                    JSONArray bindingsArray = gStoreJson.getJSONObject("results").getJSONArray("bindings");

                                    if (null == bindingsArray || bindingsArray.size() == 0) {
                                        return R.error("数据集为空");
                                    }

                                    String resultStr = "";
                                    for (int i = 0; i < bindingsArray.size(); i++) {
                                        JSONObject sub = bindingsArray.getJSONObject(i);
                                        Iterator<String> it = sub.keySet().iterator();
                                        while (it.hasNext()) {
                                            String key = it.next();
                                            String value = sub.getJSONObject(key).getString("value");
                                            resultStr = resultStr + key + ":" + value + "\n";
                                        }
                                    }
                                    return R.success(JSONObject.parseObject(resultStr));
                                } else {
                                    if (StringUtil.isNotBlank(n) && n.length() > 0) {
                                        // 高级查询
                                        return R.success(gStoreJson);
                                    } else {
                                        // 普通查询
//                                        String filePath = request.getServletContext().getRealPath("/") + "resultfile";
//                                        String filename = saveJSONFile(gStoreJson, filePath);
                                        Map iconMap = new HashMap();
                                        if (isStandard) {
                                            iconMap = KgUtils.getProjectEntityIcons(gc, dbname);
                                        }
                                        resultJson = KgUtils.genResultJson(sparql, gStoreJson, iconMap, isStandard, nodeNum);
//                                        resultJson.put("file", filename);
                                    }
                                }
                            }
                            if (sparql.toLowerCase().startsWith("ask")) {
                                String label = null;
                                if ("0.8".equals(gc.getVersion())) {
                                    label = gStoreJson.getJSONObject("results").getJSONArray("bindings").
                                            getJSONObject(0).getJSONObject("askResult").getBoolean("value") == false ? "false" : "true";
                                } else {
                                    label = gStoreJson.getJSONObject("results").getJSONArray("bindings").
                                            getJSONObject(0).getJSONObject("_askResult").getString("value");
                                }
                                return R.success(JSONObject.parseObject(label));
                            }
                        }
                    } else {
                        if (statusCode == 1005) {
                            return R.error("语法错误");
                        }
                        return R.error("查询异常：" + gStoreJson.get("StatusMsg"));
                    }
                } catch (Exception e) {
                    if (e instanceof BadRequestException) {
                        return R.error(e.getMessage());
                    }
                    log.error(e.getMessage(), e);
                    return R.error("查询失败:" + e.getMessage());
                }
            }
//            resultInfo.setData(resultJson);
            return R.success(resultJson);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error("查询失败！");
        }
//        return R.error("");
    }


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

