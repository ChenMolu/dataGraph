package com.jwlz.sjjc.zstp.utils;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jwlz.sjjc.zstp.common.Constant;
import com.jwlz.sjjc.zstp.exception.BadRequestException;
import com.jwlz.sjjc.zstp.utils.pkumod.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
public class KgUtils {
    @Value("${login.flag}")
    private static String flag;
    public static JSONArray removeduplicateLinks(JSONArray links, List<String> removeLink) {
        Map<String, String> map = new HashMap<>();
        for (int i = links.size() - 1; i >= 0; i--) {
            JSONObject link = links.getJSONObject(i);
            String key = link.getString("source") + "_" + link.getString("label") + "_" + link.getString("target");
            if (null == map.get(key)) {
                //去掉属性图的关系
                if (!removeLink.contains(key)) {
                    map.put(key, key);
                } else {
                    links.remove(i);
                }
            } else {
                links.remove(i);
            }
        }
        return links;
    }

    public static JSONArray removeduplicateNodes(JSONArray nodes, List<String> removeNodes) {
        Map<String, String> map = new HashMap<>();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            JSONObject node = nodes.getJSONObject(i);
            String key = node.getString("id");
            if (null == map.get(key)) {
                //去掉属性图的节点
                if (!removeNodes.contains(key)) {
                    map.put(key, key);
                } else {
                    nodes.remove(i);
                }
            } else {
                nodes.remove(i);
            }
        }
        return nodes;
    }

    /**
     * 从gstore获取统一的图标信息
     *
     * @param gStoreConnector
     * @param dbname
     * @return
     * @throws Exception
     */
    public static Map getProjectEntityIcons(GStoreConnector gStoreConnector, String dbname) throws Exception {
        Map map = new HashMap();
        String basesparql = "select * where {" +
                " ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#icon> ?d." +
                "}";
        JSONObject result = queryData(gStoreConnector, dbname, basesparql);
        JSONArray bindsData = null;
        bindsData = result.getJSONObject("results").getJSONArray("bindings");
        if (null == bindsData || bindsData.size() == 0) {
            return map;
        } else {
            for (int i = 0; i < bindsData.size(); i++) {
                JSONObject dJSONObject = getSubBindJson(bindsData, i);
                map.put(dJSONObject.getJSONObject("a").getString("value"), dJSONObject.getJSONObject("d").getString("value"));
            }
        }
        return map;
    }

    /**
     * 实体节点名称，换成节点show名称
     *
     * @param jsonObject 原查询结果 类似于{nodes:[],links:[]}
     * @return
     * @throws Exception
     */
    public static JSONObject nodeNameToShowName(JSONObject jsonObject, Map iconMap,GStoreConnector gc,String dbname) throws Exception {
        //此处把返回的节点 重新查询show节点

        //重新封装出发节点的id值 解决节点id不一样的问题
        JSONArray jsonArray = jsonObject.getJSONArray("nodes");
        if (null == jsonArray || jsonArray.size() <= 0) {
            return jsonObject;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            if (jsonObject1.getString("types").equals("uri")) {
                String label = jsonObject1.getJSONObject("properties").getString("value");

                if (label.indexOf("/#") != -1 && label.split("/#").length > 1) {
                    String labels = label.split("/#")[1];
                    jsonObject1.put("label", labels);
                    jsonObject1.getJSONObject("properties").put("label", labels);
                }
                if (iconMap != null && !iconMap.isEmpty()) {
                    jsonObject1.put("image", iconMap.get(label.split("/")[0]));
                }
                if (jsonObject1.containsKey("types")&&jsonObject1.getString("types").equals("uri")) {
                    //tempnode.put("size",200);
                    if (jsonObject1.containsKey("type") && jsonObject1.getString("type").equals("账户")) {
                        String id = jsonObject1.getString("id");
                        String sparql = "select ?value where {<"+id+"> <用户id> ?value.}";
                        String result = gc.query(dbname, sparql);
                        //log.info("查询sparql语句:{}\n图谱查询结果：{}耗时(ms)：{}", sparql, result, (System.currentTimeMillis() - s));
                        JSONObject json = JSONObject.parseObject(result);
                        if(json.getInteger("StatusCode")==0) {
                            JSONArray bindsData = json.getJSONObject("results").getJSONArray("bindings");
                            if (null == bindsData || bindsData.size() == 0) {
                                log.error("结果集为空");
                            } else {
                                String imageurl=bindsData.getJSONObject(0).getJSONObject("value").getString("value");
                                imageurl="images/social/"+imageurl+".jpg";
                                //imageurl="http://file.gstore.cn/seafhttp/files/12aa9519-99c6-4d7c-be8a-b5b8d9545b5b/2.jpg";
                                jsonObject1.put("image", imageurl);
                            }
                        }
                    }
                }

            }
        }
        jsonObject.put("nodes", jsonArray);
        return jsonObject;
    }


    /**
     * 节点无向 逐级探索
     *
     * @param gStoreConnector
     * @param dbname
     * @param uri              实体节点名称
     * @param direction        方向参数：1 指入;2 指出 ; 0 无向 默认：0
     * @param isStandardFormat 是否满足gbuilder抽取的标准格式 true false
     * @return
     */

    public static JSONObject stepNext(GStoreConnector gStoreConnector, String dbname, String uri, String direction, Map iconMap, boolean isStandardFormat, int nodeNum) {
        try {
            Integer limitNum = 500;
            String spasqlbase = "select * where {  {<%s> ?c ?d.}union{?a ?b <%s>} } limit " + limitNum;
            if (direction.equals("1")) {
                spasqlbase = "select * where {  ?a ?b <%s> } limit  " + limitNum;
            } else if (direction.equals("2")) {
                spasqlbase = "select * where { <%s> ?c ?d. } limit  " + limitNum;
            }
            String spasql = String.format(spasqlbase, uri, uri);
            JSONObject resultobj = queryData(gStoreConnector, dbname, spasql);
            return genResultJson(spasql, resultobj, iconMap, isStandardFormat, nodeNum);
        } catch (Exception e) {
            throw new BadRequestException("逐级探索获取数据失败：" + e.getMessage());
        }
    }

    /**
     * 查询结果过滤实体或属性，只保留实体或属性
     *
     * @param jsonObject
     * @param reservedNodeType Constant.URI 或 Constant.LITERAL
     * @return
     */
    public static JSONObject stepNextUriOrLiteral(JSONObject jsonObject, String reservedNodeType) {

        if (null == jsonObject) {
            return jsonObject;
        }

        //不需要过滤
        if (StringUtil.isBlank(reservedNodeType)) {
            return jsonObject;
        }

        JSONArray jsonArrayNodes = jsonObject.getJSONArray("nodes");
        if (null == jsonArrayNodes || jsonArrayNodes.size() <= 0) {
            return jsonObject;
        }

        //待移的节点ID
        Map<String, String> remkey = new HashMap<>();
        Map<String, String> havkey = new HashMap<>();
        Map<String, String> havBckey = new HashMap<>();
        JSONArray jsonArrayNewNodes = new JSONArray();
        //过滤出有用节点信息
        for (int i = 0; i < jsonArrayNodes.size(); i++) {
            JSONObject node = jsonArrayNodes.getJSONObject(i);
            if (node.getString("types").equals(reservedNodeType)) {
                jsonArrayNewNodes.add(node);
                havkey.put(node.getString("id"), node.getString("id"));
            } else {
                remkey.put(node.getString("id"), node.getString("id"));
            }
        }
        JSONArray jsonArrayLinks = jsonObject.getJSONArray("links");
        if (null == jsonArrayLinks || jsonArrayLinks.size() <= 0) {
            return jsonObject;
        }
        JSONArray jsonArrayNewLinks = new JSONArray();

        for (int i = 0; i < jsonArrayLinks.size(); i++) {
            JSONObject link = jsonArrayLinks.getJSONObject(i);
            //实体的关系处理
            if (reservedNodeType.equals(Constant.URI)) {
                String target = remkey.get(link.getString("target"));
                String source = remkey.get(link.getString("source"));
                if (StringUtil.isBlank(target) && StringUtil.isBlank(source)) {
                    jsonArrayNewLinks.add(link);
                }
            } else {
                //属性的处理
                String target = havkey.get(link.getString("target"));
                if (StringUtil.isNotBlank(target)) {
                    jsonArrayNewLinks.add(link);
                    String source = havkey.get(link.getString("source"));
                    if (StringUtil.isBlank(source)) {
                        havBckey.put(link.getString("source"), link.getString("source"));
                    }
                }
            }
        }
        //补充只查关系的实体节点
        if (null != havBckey && !havBckey.isEmpty() && reservedNodeType.equals(Constant.LITERAL)) {

            for (int i = 0; i < jsonArrayNodes.size(); i++) {
                JSONObject node = jsonArrayNodes.getJSONObject(i);
                String id = havBckey.get(node.getString("id"));
                if (node.getString("types").equals(Constant.URI) && StringUtil.isNotBlank(id)) {
                    jsonArrayNewNodes.add(node);
                    havkey.put(node.getString("id"), node.getString("id"));
                }
            }
        }
        jsonObject.put("nodes", jsonArrayNewNodes);
        jsonObject.put("links", jsonArrayNewLinks);
        return jsonObject;
    }

    /**
     * 清除所有的属性 保留关系
     *
     * @param resultJosn
     * @return
     */

    public static JSONObject clearProperty(JSONObject resultJosn) {
        JSONArray nodes = resultJosn.getJSONArray("nodes");
        if (null != nodes && nodes.size() > 0) {
            Map<String, String> nodeMap = new HashMap<>();
            for (int i = nodes.size() - 1; i >= 0; i--) {
                JSONObject subNode = nodes.getJSONObject(i);
                String types = subNode.getString("types");
                if (!types.contains("uri")) {
                    nodes.remove(i);
                } else {
                    nodeMap.put(subNode.getString("id"), "1");
                }
            }
            resultJosn.put("nodes", nodes);
            JSONArray links = resultJosn.getJSONArray("links");
            for (int i = links.size() - 1; i >= 0; i--) {
                JSONObject subLink = links.getJSONObject(i);
                String sourceId = subLink.getString("source");
                String targetId = subLink.getString("target");
                if (null == nodeMap.get(sourceId) || null == nodeMap.get(targetId)) {
                    links.remove(i);
                }
            }
            resultJosn.put("links", links);
        }
        return resultJosn;
    }

    /**
     * 图谱查询
     *
     * @param dbname
     * @param sparql
     * @return
     * @throws Exception
     */
    public static JSONObject queryData(GStoreConnector gStoreConnector, String dbname, String sparql) throws Exception {
        try {
            // 创建连接
            Long s = System.currentTimeMillis();
            log.info("查询sparql语句：" + sparql);
            String result = gStoreConnector.query(dbname, sparql);
            log.info("查询sparql语句:{}\n图谱查询结果：{}耗时(ms)：{}", sparql, result, (System.currentTimeMillis() - s));
            JSONObject json = JSONObject.parseObject(result);
            if (0 == json.getInteger("StatusCode")) {
                return json;
            } else {
                throw new Exception("图谱查询异常:" + json.get("StatusMsg"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("图谱查询异常：" + ex.getMessage());
            throw new Exception("图谱查询异常");
        }
    }

    /**
     * 单实体 k跳查询结果汇总
     *
     * @param gStoreConnector
     * @param dbname
     * @param sparql
     * @param jsonArray
     * @return
     */
    public static JSONArray kHopReachablePathTs(GStoreConnector gStoreConnector, String dbname, String sparql, JSONArray jsonArray) throws Exception {
        JSONObject result = queryData(gStoreConnector, dbname, sparql);

        // 查询到结果数据
        JSONArray bindsData = result.getJSONObject("results").getJSONArray("bindings");
        if (null == bindsData || bindsData.size() == 0) {
            return jsonArray;
        }

        JSONObject jsonObject = getSubBindJson(bindsData, 0);
        JSONObject jsonObject1 = jsonObject.getJSONObject("y").getJSONObject("value");
        JSONArray jsonArray1 = getSubBindJsonArray(jsonObject1, "paths");

        if (null != jsonArray1 && jsonArray1.size() > 0) {
            jsonArray.addAll(jsonArray1);
        }
        return jsonArray;
    }

    /**
     * k跳可达路径查询
     *
     * @param dbname
     * @param sparql
     * @return
     */
    public static JSONObject kHopReachablePath(GStoreConnector gStoreConnector, String dbname, String sparql, JSONObject realtionJSONObject, Map iconMap) throws Exception {
        JSONObject result = queryData(gStoreConnector, dbname, sparql);
        JSONArray jsonArray = getSeniorPaths(result.toJSONString());
        JSONObject data = seniorfunData(jsonArray, realtionJSONObject, iconMap);
        return data;
    }

    /**
     * k跳查询的结果组装前端页面
     *
     * @param jsonArray          查询结果
     * @param realtionJSONObject 具体某个项目中schema所有边 key= source-realtion-target
     *                           JSONObject realtionJSONObject = new JSONObject();
     *                           realtionJSONObject.put("信息系统-所属建设部门-部门", "信息系统-所属建设部门-部门");
     *                           realtionJSONObject.put("办理事项-参与系统-信息系统", "办理事项-参与系统-信息系统");
     * @return
     */
    public static JSONObject seniorfunData(JSONArray jsonArray, JSONObject realtionJSONObject, Map iconMap) {
        JSONArray nodes = new JSONArray();
        JSONArray links = new JSONArray();
        JSONObject havNodes = new JSONObject();
        JSONObject havLinks = new JSONObject();
        try {
            if (null != jsonArray && jsonArray.size() > 0) {
                Map<String, String> nodeNames = new HashMap<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = getSubBindJson(jsonArray, i);
                    JSONArray nodeArray = getSubBindJsonArray(object, "nodes");
                    if (null != nodeArray && nodeArray.size() > 0) {
                        for (int j = 0; j < nodeArray.size(); j++) {
                            JSONObject onode = getSubBindJson(nodeArray, j);
                            JSONObject node = new JSONObject();
                            String idValue = onode.getString("nodeIRI").replace("<", "").replace(">", "");
                            node.put("id", idValue);
                            node.put("types", Constant.URI);
                            node.put("label", idValue);
                            Map<String, Object> pmap = new HashMap<>();
                            pmap.put("id", idValue);
                            pmap.put("value", idValue);
                            pmap.put("type", Constant.URI);
                            pmap.put("label", node.getString("label"));
                            node.put("properties", pmap);
                            if (null == checkNodeIsExist(idValue, node.getString("types"), havNodes)) {
                                nodes.add(node);
                                nodeNames.put(onode.getString("nodeIndex"), node.getString("label"));
                            }
                        }
                    }

                    JSONArray linksArray = getSubBindJsonArray(object, "edges");
                    if (null != linksArray && linksArray.size() > 0) {
                        for (int j = 0; j < linksArray.size(); j++) {
                            JSONObject olink = getSubBindJson(linksArray, j);
                            JSONObject link = new JSONObject();
                            String sourceNames = nodeNames.get(olink.getString("fromNode"));
                            String targetNames = nodeNames.get(olink.getString("toNode"));
                            link.put("source", sourceNames);
                            link.put("target", targetNames);
                            link.put("label", olink.getString("predIRI").replace("<", "").replace(">", ""));
                            if (null != realtionJSONObject) {

                                String sourceName = sourceNames.substring(0, sourceNames.indexOf("/"));
                                String targetName = targetNames.substring(0, targetNames.indexOf("/"));
                                //根据schema的关系方向，做调整
                                if (StringUtil.isNotBlank(realtionJSONObject.getString(sourceName + "-" + link.getString("label") + "-" + targetName))) {
                                    link.put("source", sourceNames);
                                    link.put("target", targetNames);
                                } else if (StringUtil.isNotBlank(realtionJSONObject.getString(targetName + "-" + link.getString("label") + "-" + sourceName))) {
                                    link.put("target", sourceNames);
                                    link.put("source", targetNames);
                                }
                            }
                            if (!checkLinkIsExist(link, havLinks)) {
                                links.add(link);
                                // System.err.println(nodeNames.get(link.getString("source")) + " -> " + link.getString("label") + " ->  " + nodeNames.get(link.getString("target")));
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("解析数据异常:" + e.getMessage());
        }
        //替换显示值 和替换图标
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            //替换图片
            if (null != iconMap && !iconMap.isEmpty() && node.getString("types").equals(Constant.URI)) {
                String kname = node.getJSONObject("properties").getString("value").split("/")[0];
                if (StringUtil.isNotBlank(kname)) {
                    node.put("image", iconMap.get(kname));
                }
            }
        }
        JSONObject data = new JSONObject();
        data.put("links", links);
        data.put("nodes", nodes);
        return data;
    }

    public static void main(String[] args) {
        GStoreConnector gc = new GStoreConnector10("8.142.21.15", 20028, "root", "123456", Constant.GRPC);

        try {
            Result data = run(gc, "狂飙", "select * where {?a ?b ?c}");
            System.err.println(data.listToJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 通用执行
     *
     * @param gc
     * @param dbName
     * @param sparql
     * @return
     * @throws Exception
     */
    public static Result run(GStoreConnector gc, String dbName, String sparql) throws Exception {
        gc.load(dbName);
        String result_str = gc.query(dbName, sparql);
        return Result.instance().build(result_str);
    }

//    public boolean testConnect() {
//        Map<String, String> map = new HashMap<>();
//        map.put("operation", "login");
//        map.put("username", this.username);
//        map.put("password", this.password);
//        String result_str = sendPost(this.uri, JSON.toJSONString(map), timeout);
//        Result rt = null;
//        try {
//            rt = Result.instance().build(result_str);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (rt != null) {
//            return rt.success();
//        } else {
//            return false;
//        }
//    }




    public static JSONArray changeImageUri(JSONArray nodes,GStoreConnector gStoreConnector, String dbname,String imagePropertyName)
    {
        JSONArray resultnode=new JSONArray();
        if(imagePropertyName!=null&&imagePropertyName.isEmpty()==false) {
            for (int i = 0; i < nodes.size(); i++) {
                JSONObject tempnode = nodes.getJSONObject(i);
                if (tempnode.containsKey("types")&&tempnode.getString("types").equals("uri")) {
                   tempnode.put("size",200);
                    if (tempnode.containsKey("type") && tempnode.getString("type").equals("账户")) {
                        String id = tempnode.getString("id");
                        String sparql = "select ?value where {<"+id+"> <"+imagePropertyName+"> ?value.}";
                        String result = gStoreConnector.query(dbname, sparql);
                        //log.info("查询sparql语句:{}\n图谱查询结果：{}耗时(ms)：{}", sparql, result, (System.currentTimeMillis() - s));
                        JSONObject json = JSONObject.parseObject(result);
                        if(json.getInteger("StatusCode")==0) {
                            JSONArray bindsData = json.getJSONObject("results").getJSONArray("bindings");
                            if (null == bindsData || bindsData.size() == 0) {
                                log.error("结果集为空");
                            } else {
                                String imageurl=bindsData.getJSONObject(0).getJSONObject("value").getString("value");
                                imageurl="images/social/"+imageurl+".jpg";
                                //imageurl="http://file.gstore.cn/seafhttp/files/12aa9519-99c6-4d7c-be8a-b5b8d9545b5b/2.jpg";
                                tempnode.put("image", imageurl);
                            }
                        }
                    }
                }

                resultnode.add(tempnode);

            }
            return resultnode;
        }
        else {
          return nodes;
        }
    }
    /**
     * 解析sparql、图数据库查询结果构造数据
     *
     * @param queryString      查询的sparql
     * @param result           查询的结果
     * @param iconMap          图标map
     * @param isStandardFormat 是否满足gbuilder标准格式
     * @param nodeNum 后台计算xy坐标的
     * @return
     */
    public static JSONObject genResultJson(String queryString, JSONObject result, Map iconMap, boolean isStandardFormat, int nodeNum) {

        JSONObject resultJson = new JSONObject();
        JSONArray simpleJson = new JSONArray();
        try {
            // 查询到结果数据buildings bindings
            JSONArray bindsData = result.getJSONObject("results").getJSONArray("bindings");
            if (null == bindsData || bindsData.size() == 0) {
                resultJson.put("size", 0);
                resultJson.put("code", "1");
                resultJson.put("msg", "数据集为空");
                throw new BadRequestException("数据集为空");
            } else {

                for (int i = 0; i < bindsData.size(); i++) {
                    simpleJson.add(getSubBindJson(bindsData, i));
                }

                resultJson.put("size", bindsData.size());
                Query query = QueryFactory.create(queryString);
                JSONArray nodes = new JSONArray();
                JSONArray links = new JSONArray();

                //去重主谓宾
                JSONObject duplicate = new JSONObject();
                Long s = System.currentTimeMillis();

                //替换显示字段
                List<Element> elementList = getElementList(query);
                List<TriplePath> pathList = new ArrayList<>();
                for (Element element : elementList) {
                    if (element instanceof ElementGroup) {
                        ElementGroup group = (ElementGroup) element;
                        List<Element> elementList1 = group.getElements();
                        for (Element element1 : elementList1) {
                            if (element1 instanceof ElementPathBlock) {
                                ElementPathBlock block = (ElementPathBlock) element1;
                                // 获取三元组信息
                                pathList.addAll(block.getPattern().getList());
                            }
                        }
                    }
                    if (element instanceof ElementPathBlock) {
                        ElementPathBlock block = (ElementPathBlock) element;
                        // 获取三元组信息
                        pathList.addAll(block.getPattern().getList());
                    }
                }
                //存已有节点 做去重判断
                JSONObject havNodes = new JSONObject();
                //存已有关系 做去重判断
                JSONObject havLinks = new JSONObject();

                JSONObject obj = toPackageNodesAndLinks(havNodes, havLinks, pathList, query, bindsData, nodes, links, duplicate, isStandardFormat);
                nodes = obj.getJSONArray("nodes");
                links = obj.getJSONArray("links");
//                duplicate = obj.getJSONObject("duplicate");

                Map<String, String> nodeMap = new HashMap<>();
                double area = Math.max(Math.sqrt(nodes.size()) * 50, 10 * 50);
//                double initialAngle = 60 * Math.PI * (3 - Math.sqrt(6));
                double initialAngle = 20 * Math.PI * (3 - Math.sqrt(6));

                // 是否后台进行坐标计算
                boolean isCalculateXY = false;
                if(nodes.size() > nodeNum) {
                    isCalculateXY = true;
                }
                //替换显示值 和替换图标
                for (int i = 0; i < nodes.size(); i++) {
                    JSONObject node = nodes.getJSONObject(i);
                    //替换图片
                    if (null != iconMap && !iconMap.isEmpty() && node.getString("types").equals(Constant.URI)) {
                        String kname = node.getJSONObject("properties").getString("value").split("/")[0];
                        if (StringUtil.isNotBlank(kname)) {
                            node.put("image", iconMap.get(kname));
                        }
                    }
                    if(isCalculateXY) {
                        double angles = i * initialAngle;
                        double x = Math.round(Math.random() * area * 2) * Math.cos(angles);
                        double y = Math.round(Math.random() * area) * Math.sin(angles);
                        node.put("x", x);
                        node.put("y", y);
                    }
                    nodeMap.put(node.getString("id"), node.getString("label"));
                }

                //处理谓语是主语的情况
                /**
                 * 找出所有谓语
                 * 谓语与主语能匹配上
                 * 组装谓语当主语的详细参数
                 * 把详细参数填回关系上
                 * 去掉主语节点和属性节点
                 * 去掉关系
                 */
                Map<String, Object> linkMap = new HashMap();
                links.forEach(item -> {
                    JSONObject link = (JSONObject) item;
                    linkMap.put(link.getString("label"), link);
                });

                List<String> removeNodes = new ArrayList<>();
                List<String> removeLink = new ArrayList<>();
                //找出谓语是主语的数据
                Map<String, JSONObject> wyMap = new HashMap<>();
                nodes.forEach(item -> {
                    JSONObject node = (JSONObject) item;
                    JSONObject link = (JSONObject) linkMap.get(node.getString("id"));
                    if (null != link && node.getString("types").equals(Constant.URI)) {
                        wyMap.put(link.getString("label"), link);

                    }
                });

                havLinks.forEach((k, v) -> {
                    String[] keys = k.split("_");
                    JSONObject link = wyMap.get(keys[0]);
                    if (null != link) {
                        removeNodes.add(keys[0]);
                        removeNodes.add(keys[2]);
                        removeLink.add(k);
                        //详细参数
                        JSONObject pp = link.getJSONObject(keys[0]);
                        if (pp == null) {
                            pp = new JSONObject();
                        }
                        pp.put(keys[1], nodeMap.get(keys[2]));
                        link.put("properties", pp);
                        link.put(keys[0], pp);

                    }
                });

                resultJson.put("nodes", removeduplicateNodes(nodes, removeNodes));
                resultJson.put("links", removeduplicateLinks(links, removeLink));
                resultJson.put("isCalculateXY", isCalculateXY);
                log.info("节点数量:{},关系数量:{},解析结果耗时(ms):{}", nodes.size(), links.size(), (System.currentTimeMillis() - s));

            }
        } catch (
                Exception e) {
            if (e instanceof BadRequestException) {
                throw e;
            }
            log.error("图谱查询结果解析错误：" + e.getMessage(), e);
            throw new BadRequestException("图谱查询结果解析错误：" + e.getMessage());
        }

        JSONObject oldJson = (JSONObject) result.clone();
        oldJson.getJSONObject("results").put("bindings", simpleJson);
        resultJson.put("data2", oldJson);

        return resultJson;
    }


    /**
     * 新 布局算法
     *
     * @param
     * @return
     */
//    public static JSONObject runJGraphLayout(JSONObject jsonObject) {
//        Long s = System.currentTimeMillis();
//        mxGraph graph = new mxGraph();
//        Object parent = graph.getDefaultParent();
//
//        graph.getModel().beginUpdate();
//        JSONObject dataResult = new JSONObject();
//        dataResult.putAll(jsonObject);
//
//        try {
//            JSONArray nodesArray = jsonObject.getJSONArray("nodes");
//            Object[] nodes = new Object[nodesArray.size()];
//
//            Map<String, Object> verticesMap = new HashMap();
//            Map<String, Object> nodeMap = new HashMap();
//            for (int i = 0; i < nodesArray.size(); i++) {
//                JSONObject items = nodesArray.getJSONObject(i);
//                nodes[i] = graph.insertVertex(parent, items.getString("id"), items.getString("label"), 0, 0, 100,
//                        100);
//
//                verticesMap.put(items.getString("id"), nodes[i]);
//                nodeMap.put(items.getString("id"), items);
//            }
//
//            JSONArray linksArray = jsonObject.getJSONArray("links");
//            Object[] edges = new Object[linksArray.size()];
//            for (int i = 0; i < linksArray.size(); i++) {
//                JSONObject items = linksArray.getJSONObject(i);
//                edges[i] = graph.insertEdge(parent, items.getString("id"), items.getString("label"),
//                        verticesMap.get(items.getString("source")), verticesMap.get(items.getString("target")));
//            }
//
//            mxIGraphLayout layout = new mxFastOrganicLayout(graph);
////            mxIGraphLayout layout = new mxStackLayout(graph);
//            layout.execute(parent);
//
//            JSONArray newNodesArray = new JSONArray();
//            for (int j = 0; j < nodes.length; j++) {
//                mxCell cellss = (mxCell) nodes[j];
//                if (!cellss.isEdge()) {
////                System.err.println(j + " " + cellss.getId() + " " + cellss.getValue() + " " + JSON.toJSONString(cellss.getGeometry()));
//                    JSONObject node = (JSONObject) nodeMap.get(cellss.getId());
//                    node.put("x", cellss.getGeometry().getX());
//                    node.put("y", cellss.getGeometry().getY());
//                    newNodesArray.add(node);
//                }
//            }
//
//            dataResult.put("nodes", newNodesArray);
//            dataResult.put("links", linksArray);
//        } finally {
//            graph.getModel().endUpdate();
//            log.info("布局耗时：{}", (System.currentTimeMillis() - s));
//
//        }
//
//        return dataResult;
//    }
    public static JSONObject kHopEPath(JSONArray jsonArray, JSONObject realtionJSONObject, Map iconMap) {
        JSONArray nodes = new JSONArray();
        JSONArray links = new JSONArray();
        JSONArray pathArray = new JSONArray();
        JSONObject havNodes = new JSONObject();
        JSONObject havLinks = new JSONObject();
        try {
            if (null != jsonArray && jsonArray.size() > 0) {
                Map<String, String> nodeNames = new HashMap<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = getSubBindJson(jsonArray, i);
                    String src = object.getString("src").replace("<", "").replace(">", "");
                    String dst = object.getString("dst").replace("<", "").replace(">", "");
                    JSONObject pathObject = new JSONObject();
                    JSONArray subNodes = new JSONArray();
                    JSONArray subLinks = new JSONArray();
                    pathObject.put("nodes", subNodes);
                    pathObject.put("links", subLinks);

                    JSONArray nodeArray = getSubBindJsonArray(object, "nodes");
                    if (null != nodeArray && nodeArray.size() > 0) {
                        for (int j = 0; j < nodeArray.size(); j++) {
                            JSONObject onode = getSubBindJson(nodeArray, j);
                            JSONObject node = new JSONObject();
                            String idValue = onode.getString("nodeIRI").replace("<", "").replace(">", "");
                            if (src.equals(idValue) || dst.equals(idValue)) {
                                node.put("level", "1");
                            }
                            node.put("id", idValue);
                            node.put("types", Constant.URI);
                            node.put("label", idValue);
                            Map<String, Object> pmap = new HashMap<>();
                            pmap.put("id", idValue);
                            pmap.put("value", idValue);
                            pmap.put("type", Constant.URI);
                            pmap.put("label", node.getString("label"));
                            node.put("properties", pmap);
                            subNodes.add(node);
                            if (null == checkNodeIsExist(idValue, node.getString("types"), havNodes)) {
                                nodes.add(node);
                                nodeNames.put(onode.getString("nodeIndex"), node.getString("label"));
                            }
                        }
                    }

                    JSONArray linksArray = getSubBindJsonArray(object, "edges");
                    if (null != linksArray && linksArray.size() > 0) {
//                        Map<String, String> linkPath = new HashMap<>();
                        String pathLabel = "";
                        String pre = "";
                        String next = "";
//                        System.err.println(linksArray);
                        for (int j = 0; j < linksArray.size(); j++) {
                            JSONObject olink = getSubBindJson(linksArray, j);
                            JSONObject link = new JSONObject();
                            String sourceNames = nodeNames.get(olink.getString("fromNode"));
                            String targetNames = nodeNames.get(olink.getString("toNode"));
                            link.put("source", sourceNames);
                            link.put("target", targetNames);
                            link.put("label", olink.getString("predIRI").replace("<", "").replace(">", ""));

                            subLinks.add(link);
//                            linkPath.put(link.getString("source"), link.getString("target"));
                            String label = link.getString("label");
//                            linkPath.put(link.getString("source") + "_" + link.getString("target"), label);
                            if (!checkLinkIsExist(link, havLinks)) {
                                links.add(link);
                            }
//                             System.err.println(j+" "+link.getString("source") + "→" + label + "→" + link.getString("target"));

                            if (j == 0) {
                                pathLabel += link.getString("source") + "→" + label + "→" + link.getString("target");

                            } else {
                                if (link.getString("target").equals(pre)) {
                                    if (pathLabel.indexOf(pre) == 0) {
                                        pathLabel = link.getString("source") + "→" + label + "→" + pathLabel;
                                    } else {
                                        pathLabel = pathLabel + "←" + label + "←" + link.getString("source");
                                    }
                                } else if (link.getString("source").equals(next)) {
                                    if (pathLabel.indexOf(next) == 0) {
                                        pathLabel = link.getString("source") + "→" + label + "→" + pathLabel;
                                    } else {
                                        pathLabel = pathLabel + "→" + label + "→" + link.getString("target");

                                    }
                                } else if (link.getString("source").equals(pre)) {
                                    if (pathLabel.indexOf(pre) == 0) {
                                        pathLabel = link.getString("target") + "←" + label + "←" + pathLabel;

                                    } else {
                                        pathLabel = pathLabel + "→" + label + "→" + link.getString("target");

                                    }
                                } else if (link.getString("target").equals(next)) {
                                    if (pathLabel.indexOf(next) == 0) {
                                        pathLabel = link.getString("source") + "→" + label + "→" + pathLabel;
                                    } else {
                                        pathLabel = pathLabel + "←" + label + "←" + link.getString("source");
                                    }

                                }
                            }

                            pre = link.getString("source");
                            next = link.getString("target");
                        }

//                        String source = src;
//                        String pathLabel = source;
//                        while (true) {
//                            String target = linkPath.get(source);
//                            pathLabel = pathLabel + "->" + linkPath.get(source + "_" + target) + "->" + target;
//                            if (dst.equals(target)) {
//                                break;
//                            }
//                            source = target;
//                        }

//                        System.err.println(pathLabel);
                        pathObject.put("pathLabel", pathLabel);
                        pathObject.put("id", CommonUtils.getUUID());
                    }

                    if (subNodes.size() > 0 || subLinks.size() > 0) {
                        pathArray.add(pathObject);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("解析数据异常:" + e.getMessage());
        }
        JSONArray newNodes = new JSONArray();
        //替换显示值 和替换图标
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            //替换图片
            if (null != iconMap && !iconMap.isEmpty() && node.getString("types").equals(Constant.URI)) {
                String kname = node.getJSONObject("properties").getString("value").split("/")[0];
                if (StringUtil.isNotBlank(kname)) {
                    node.put("image", iconMap.get(kname));
                }
            }
            newNodes.add(node);
        }
        JSONObject data = new JSONObject();
        data.put("links", links);
        data.put("nodes", newNodes);
        data.put("paths", pathArray);
        return data;
    }


    /**
     * 通过解析组装可视化数据
     *
     * @param pathList         知识图谱的三元组：<subject, predicate/relation, object>
     * @param query
     * @param bindsData        查询返回数据
     * @param nodes            封装节点
     * @param links            封装关系
     * @param duplicate        去重主谓宾
     * @param isStandardFormat 是否满足gbuilder标准格式 true false
     * @return
     */
    public static JSONObject toPackageNodesAndLinks(JSONObject havNodes, JSONObject havLinks, List<TriplePath> pathList, Query query, JSONArray bindsData, JSONArray nodes, JSONArray links, JSONObject duplicate, boolean isStandardFormat) {
        JSONObject data = new JSONObject();


        // 知识图谱的三元组：<subject, predicate/relation, object>
        for (TriplePath triplePath : pathList) {
            // 主语
            Node subjectNode = triplePath.getSubject();
            // 谓语
            Node predicateNode = triplePath.getPredicate();
            // 宾语
            Node objectNode = triplePath.getObject();

            // 通过宾语和谓语查主语，且宾语不是对象  ?a <喜欢> "睡觉"
            if (subjectNode.isVariable() && !objectNode.isVariable() && !objectNode.isURI() && predicateNode.isConcrete()) {
                //找出主语参数
                String subjectName = ((Var) triplePath.getSubject()).getVarName();
                //找出谓语
                String predicateVal = predicateNode.getURI().replace(query.getResolver().getBaseIRIasString(), "");
                //找出宾语
                String objectVal = objectNode.toString().replace(query.getResolver().getBaseIRIasString(), "").replace("\"", "");
                String objectType = objectNode.isURI() ? Constant.URI : Constant.LITERAL;

                //宾语节点 空节点新增
                String label = objectVal.replace("\"", "");
                if (label.indexOf("^^") != -1) {
                    label = label.split("\\^\\^")[0];
                }

                nodes = addNodes(havNodes, nodes, objectVal, label, objectVal, objectType, "", objectVal, isStandardFormat);


                for (int i = 0; i < bindsData.size(); i++) {
                    JSONObject dJSONObject = getSubBindJson(bindsData, i);
                    JSONObject subject = dJSONObject.getJSONObject(subjectName);
                    if (null == subject) {
                        continue;
                    }

                    //剔除相同的主谓宾
                    StringBuffer key = new StringBuffer(subject.getString("value")).append("_").append(subject.getString("type")).append("_").append(predicateVal).append("_").append(objectVal).append("_").append(objectType);
                    if (null != checkSPO(duplicate, key.toString())) {
                        continue;
                    }

                    //并判断主语是否存在
                    //新增主语节点
                    nodes = addNodes(havNodes, nodes, subject.getString("value"), subject.getString("value"), subject.getString("value"), subject.getString("type"), subject.containsKey("datatype") ? subject.getString("datatype") : "", subjectName, isStandardFormat);


                    links = addLinks(links, havLinks, subject.getString("value"), objectVal, predicateVal);

                }
                continue;
            }
            // 通过宾语和谓语查主语，且宾语是对象 ?a <喜欢> <欢欢>
            if (subjectNode.isVariable() && !objectNode.isVariable() && objectNode.isURI() && predicateNode.isConcrete()) {
                //找出主语参数
                String subjectName = ((Var) triplePath.getSubject()).getVarName();
                //找出谓语
                String predicateVal = predicateNode.getURI().replace(query.getResolver().getBaseIRIasString(), "");
                //找出宾语
                String objectVal = objectNode.getURI().replace(query.getResolver().getBaseIRIasString(), "").replace("\"", "");

                String objectType = objectNode.isURI() ? Constant.URI : Constant.LITERAL;


                //宾语空节点新增
                nodes = addNodes(havNodes, nodes, objectVal, objectVal, objectVal, objectType, "", objectVal, isStandardFormat);

                for (int i = 0; i < bindsData.size(); i++) {
                    JSONObject dJSONObject = getSubBindJson(bindsData, i);
                    JSONObject subject = dJSONObject.getJSONObject(subjectName);
                    if (subject == null) {
                        continue;
                    }
                    //剔除相同的主谓宾
                    StringBuffer key = new StringBuffer(subject.getString("value")).append("_").append(subject.getString("type")).append("_").append(predicateVal).append("_").append(objectVal).append("_").append(objectType);
                    if (null != checkSPO(duplicate, key.toString())) {
                        continue;
                    }
                    //并判断主语是否存在
                    //新增主语节点
                    nodes = addNodes(havNodes, nodes, subject.getString("value"), subject.getString("value"), subject.getString("value"), subject.getString("type"), subject.containsKey("datatype") ? subject.getString("datatype") : "", subjectName, isStandardFormat);

                    links = addLinks(links, havLinks, subject.getString("value"), objectVal, predicateVal);

                }
                continue;
            }

            // 通过主语和宾语查谓语，且宾语是对象 <波波> ?a <游泳>
            if (subjectNode.isURI() && predicateNode.isVariable() && objectNode.isURI()) {
                //找出主语
                String subjectVal = subjectNode.getURI().replace(query.getResolver().getBaseIRIasString(), "");
                String subjectType = subjectNode.isURI() ? Constant.URI : Constant.LITERAL;


                //找出谓语
                String predicateName = ((Var) triplePath.getPredicate()).getVarName();

                //找出宾语
                String objectVal = objectNode.getURI().replace(query.getResolver().getBaseIRIasString(), "");
                String objectType = objectNode.isURI() ? Constant.URI : Constant.LITERAL;

                //并判断主语是否存在
                //新增节点
                nodes = addNodes(havNodes, nodes, subjectVal, subjectVal, subjectVal, subjectType, "", subjectVal, isStandardFormat);


                //新增节点
                nodes = addNodes(havNodes, nodes, objectVal, objectVal, objectVal, objectType, "", objectVal, isStandardFormat);


                for (int i = 0; i < bindsData.size(); i++) {
                    JSONObject dJSONObject = getSubBindJson(bindsData, i);
                    JSONObject predicate = dJSONObject.getJSONObject(predicateName);
                    if (null == predicate) {
                        continue;
                    }
                    if (predicate.getString("value").equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#icon") && predicate.getString("value") != null) {
                        continue;
                    }

                    //剔除相同的主谓宾
                    StringBuffer key = new StringBuffer(subjectVal).append("_").append(subjectType).append("_").append(predicate.getString("value")).append("_").append(objectVal).append("_").append(objectType);
                    if (null != checkSPO(duplicate, key.toString())) {
                        continue;
                    }

                    links = addLinks(links, havLinks, subjectVal, objectVal, predicate.getString("value"));
                }


                continue;
            }

            // 通过主语和谓语查宾语,且主语是对象 <波波> <喜欢> ?a
            if (!subjectNode.isVariable() && objectNode.isVariable() && subjectNode.isURI() && predicateNode.isConcrete()) {
                //找出主语
                String subjectVal = subjectNode.getURI().replace(query.getResolver().getBaseIRIasString(), "");
                String subjectType = subjectNode.isURI() ? Constant.URI : Constant.LITERAL;
                //找出谓语
                String predicateVal = predicateNode.getURI().replace(query.getResolver().getBaseIRIasString(), "");
                //找出宾语的参数
                String objectName = ((Var) triplePath.getObject()).getVarName();

                //并判断主语是否存在
                //新增节点
                nodes = addNodes(havNodes, nodes, subjectVal, subjectVal, subjectVal, subjectType, "", subjectVal, isStandardFormat);

                //找出谓语、宾语的值
                for (int i = 0; i < bindsData.size(); i++) {
                    JSONObject dJSONObject = getSubBindJson(bindsData, i);
                    JSONObject object = dJSONObject.getJSONObject(objectName);
                    if (null == object) {
                        continue;
                    }
                    //宾语节点

                    String id = UUID.randomUUID().toString();
                    //宾语是实体
                    if (object.getString("type").equals(Constant.URI)) {
                        id = object.getString("value");
                    }

                    //剔除相同的主谓宾
                    StringBuffer key = new StringBuffer(subjectVal).append("_").append(subjectType).append("_").append(predicateVal).append("_").append(object.getString("value")).append("_").append(object.getString("type"));
                    if (null != checkSPO(duplicate, key.toString())) {
                        continue;
                    }
                    //空节点不显示
                    if (StringUtil.isNotBlank(object.getString("value"))) {
                        nodes = addNodes(havNodes, nodes, id, object.getString("value"), object.getString("value"), object.getString("type"), object.containsKey("datatype") ? object.getString("datatype") : "", objectName, isStandardFormat);

                        links = addLinks(links, havLinks, subjectVal, id, predicateVal);

                    }
                }

                continue;
            }
            // 主语和宾语都是变量  ?a <喜欢> ?c
            if (subjectNode.isVariable() && objectNode.isVariable() && predicateNode.isConcrete()) {
                String subjectName = ((Var) triplePath.getSubject()).getVarName();
                String objectName = ((Var) triplePath.getObject()).getVarName();

                String predicateVal = predicateNode.getURI().replace(query.getResolver().getBaseIRIasString(), "");

                for (int i = 0; i < bindsData.size(); i++) {
                    JSONObject dJSONObject = getSubBindJson(bindsData, i);
                    JSONObject subject = dJSONObject.getJSONObject(subjectName);
                    JSONObject object = dJSONObject.getJSONObject(objectName);
                    if (subject == null || object == null) {
                        //只一个条数据 并且只有主语 没有谓语 或只有谓语 没有主语
                        if (subject != null && object == null) {
                            nodes = addNodes(havNodes, nodes, subject.getString("value"), subject.getString("value"), subject.getString("value"), subject.getString("type"), subject.containsKey("datatype") ? subject.getString("datatype") : "", subjectName, isStandardFormat);
                        } else if (subject == null && object != null) {//只有谓语节点
                            String id = UUID.randomUUID().toString();
                            if (object.getString("type").equals(Constant.URI)) {
                                id = object.getString("value");
                            }
                            nodes = addNodes(havNodes, nodes, id, object.getString("value"), object.getString("value"), object.getString("type"), object.containsKey("datatype") ? object.getString("datatype") : "", objectName, isStandardFormat);
                        }
                        continue;
                    }


                    //剔除相同的主谓宾
                    StringBuffer key = new StringBuffer(subject.getString("value")).append("_").append(subject.getString("type")).append("_").append(predicateVal).append("_").append(object.getString("value")).append("_").append(object.getString("type"));
                    if (null != checkSPO(duplicate, key.toString())) {
                        continue;
                    }


                    //新增主语节点
                    nodes = addNodes(havNodes, nodes, subject.getString("value"), subject.getString("value"), subject.getString("value"), subject.getString("type"), subject.containsKey("datatype") ? subject.getString("datatype") : "", subjectName, isStandardFormat);

                    //宾语是实体
                    String id = UUID.randomUUID().toString();
                    if (object.getString("type").equals(Constant.URI)) {
                        id = object.getString("value");
                    }
                    // //宾语节点 空节点不显示

                    nodes = addNodes(havNodes, nodes, id, object.getString("value"), object.getString("value"), object.getString("type"), object.containsKey("datatype") ? object.getString("datatype") : "", objectName, isStandardFormat);

                    links = addLinks(links, havLinks, subject.getString("value"), id, predicateVal);


                }

                continue;
            }
            //主谓宾都是变量 ?a ?b ?c
            if (subjectNode.isVariable() && objectNode.isVariable() && predicateNode.isVariable()) {
                String subjectName = ((Var) triplePath.getSubject()).getVarName();
                String objectName = ((Var) triplePath.getObject()).getVarName();
                String predicateName = ((Var) triplePath.getPredicate()).getVarName();
                for (int i = 0; i < bindsData.size(); i++) {
                    JSONObject dJSONObject = getSubBindJson(bindsData, i);

                    JSONObject subject = dJSONObject.getJSONObject(subjectName);

                    JSONObject predicate = dJSONObject.getJSONObject(predicateName);
                    JSONObject object = dJSONObject.getJSONObject(objectName);
                    //只查主语
                    if (object == null && predicate == null && subject != null) {
                        //新增主语节点
                        nodes = addNodes(havNodes, nodes, subject.getString("value"), subject.getString("value"), subject.getString("value"), subject.getString("type"), subject.containsKey("datatype") ? subject.getString("datatype") : "", subjectName, isStandardFormat);
                        continue;
                    }
                    //只查宾语
                    if (object != null && predicate == null && subject == null) {

                        String id = UUID.randomUUID().toString();
                        //宾语是实体
                        if (object.getString("type").equals(Constant.URI)) {
                            id = object.getString("value");
                        }
                        //空节点不显示
                        if (StringUtil.isNotBlank(object.getString("value"))) {

                            nodes = addNodes(havNodes, nodes, id, object.getString("value"), object.getString("value"), object.getString("type"), object.containsKey("datatype") ? object.getString("datatype") : "", objectName, isStandardFormat);
                        }
                        continue;
                    }
                    //宾语为空 直接返回
                    if (object == null || predicate == null || subject == null) {
                        continue;
                    }

                    if (predicate.getString("value").equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#icon") && object.getString("value") != null) {
                        continue;
                    }

                    //剔除相同的主谓宾
                    StringBuffer key = new StringBuffer(subject.getString("value")).append("_").append(subject.getString("type")).append("_").append(predicate.getString("value")).append("_").append(object.getString("value")).append("_").append(object.getString("type"));
                    if (null != checkSPO(duplicate, key.toString())) {
                        continue;
                    }


                    //新增主语节点
                    nodes = addNodes(havNodes, nodes, subject.getString("value"), subject.getString("value"), subject.getString("value"), subject.getString("type"), subject.containsKey("datatype") ? subject.getString("datatype") : "", subjectName, isStandardFormat);


                    //宾语节点

                    String id = UUID.randomUUID().toString();
                    //宾语是实体
                    if (object.getString("type").equals(Constant.URI)) {
                        id = object.getString("value");
                    }
                    //空节点不显示
                    if (StringUtil.isNotBlank(object.getString("value"))) {

                        nodes = addNodes(havNodes, nodes, id, object.getString("value"), object.getString("value"), object.getString("type"), object.containsKey("datatype") ? object.getString("datatype") : "", objectName, isStandardFormat);


                        links = addLinks(links, havLinks, subject.getString("value"), id, predicate.getString("value"));

                    }

                }
                continue;
            }
            //主谓都是变量，宾语是对象 ?a ?b <游泳>
            if (subjectNode.isVariable() && predicateNode.isVariable() && !objectNode.isVariable()) {
                String subjectName = ((Var) triplePath.getSubject()).getVarName();
                String predicateName = ((Var) triplePath.getPredicate()).getVarName();

                String objectVal = objectNode.getURI().replace(query.getResolver().getBaseIRIasString(), "");
                String objectType = objectNode.isURI() ? Constant.URI : Constant.LITERAL;

                for (int i = 0; i < bindsData.size(); i++) {
                    JSONObject dJSONObject = getSubBindJson(bindsData, i);
                    JSONObject subject = dJSONObject.getJSONObject(subjectName);
                    JSONObject predicate = dJSONObject.getJSONObject(predicateName);

                    //宾语为空 直接返回
                    if (objectVal == null || predicate == null || subject == null) {
                        continue;
                    }

                    if (predicate.getString("value").equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#icon") && objectVal != null) {
                        continue;
                    }
                    //剔除相同的主谓宾
                    StringBuffer key = new StringBuffer(subject.getString("value")).append("_").append(subject.getString("type")).append("_").append(predicate.getString("value")).append("_").append(objectVal).append("_").append(objectType);
                    if (null != checkSPO(duplicate, key.toString())) {
                        continue;
                    }


                    //新增主语节点
                    nodes = addNodes(havNodes, nodes, subject.getString("value"), subject.getString("value"), subject.getString("value"), subject.getString("type"), subject.containsKey("datatype") ? subject.getString("datatype") : "", subjectName, isStandardFormat);


                    //宾语节点
                    String id = UUID.randomUUID().toString();
                    //宾语是实体
                    if (objectType.equals(Constant.URI)) {
                        id = objectVal;
                    }
                    //空节点不显示
                    if (StringUtil.isNotBlank(objectVal)) {

                        nodes = addNodes(havNodes, nodes, id, objectVal, objectVal, objectType, "", objectVal, isStandardFormat);

                        links = addLinks(links, havLinks, subject.getString("value"), id, predicate.getString("value"));

                    }

                }
                continue;
            }
            // 谓语和宾语是变量 <波波> ?a ?b
            if (!subjectNode.isVariable() && predicateNode.isVariable() && objectNode.isVariable()) {
                //找出主语，并判断主语是否存在
                String subjectVal = subjectNode.getURI().replace(query.getResolver().getBaseIRIasString(), "");
                String subjectType = subjectNode.isURI() ? Constant.URI : Constant.LITERAL;

                //判断主语节点不存在
                //新增节点
                nodes = addNodes(havNodes, nodes, subjectVal, subjectVal, subjectVal, subjectType, "", subjectVal, isStandardFormat);


                //找出谓语 、 宾语的参数
                String predicateName = ((Var) triplePath.getPredicate()).getVarName();
                String objectName = ((Var) triplePath.getObject()).getVarName();

                //找出谓语、宾语的值
                for (int i = 0; i < bindsData.size(); i++) {
                    JSONObject dJSONObject = getSubBindJson(bindsData, i);
                    JSONObject predicate = dJSONObject.getJSONObject(predicateName);
                    JSONObject object = dJSONObject.getJSONObject(objectName);
                    //宾语为空 直接返回
                    if (object == null || predicate == null) {
                        continue;
                    }

                    if (predicate.getString("value").equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#icon") && object.getString("value") != null) {
                        continue;
                    }
                    //宾语节点
                    String id = UUID.randomUUID().toString();
                    //宾语是实体
                    if (object.getString("type").equals(Constant.URI)) {
                        id = object.getString("value");
                    }
                    //剔除相同的主谓宾
                    StringBuffer key = new StringBuffer(subjectVal).append("_").append(subjectType).append("_").append(predicate.getString("value")).append("_").append(object.getString("value")).append("_").append(object.getString("type"));
                    if (null != checkSPO(duplicate, key.toString())) {
                        continue;
                    }

                    //空节点不显示
                    if (StringUtil.isNotBlank(object.getString("value"))) {
                        nodes = addNodes(havNodes, nodes, id, object.getString("value"), object.getString("value"), object.getString("type"), object.containsKey("datatype") ? object.getString("datatype") : "", objectName, isStandardFormat);
                        links = addLinks(links, havLinks, subjectVal, id, predicate.getString("value"));
                    }
                }
            }
            // 其他情况业务上不需要
            continue;
        }
        data.put("nodes", nodes);
        data.put("links", links);
        data.put("duplicate", duplicate);
        return data;

    }

    //添加节点
    public static JSONArray addNodes(JSONObject havNodes, JSONArray nodes, String id, String label, String value, String type, String datatype, String varName, Boolean isStandardFormat) {
        //判断节点 不存在 才进入
        if (checkNodeIsExist(value, type, havNodes) == null) {
            //新增节点
            JSONObject nodeObject = new JSONObject();

            if (type.equals(Constant.URI) && label.indexOf("/#") != -1 && isStandardFormat) {
                //实体的显示字段的值为空，显示为原数据
                String[] labelArray = label.split("/#");
                if (labelArray.length > 1) {
                    label = labelArray[labelArray.length - 1];
                }
            }
            //当前参数名
            nodeObject.put("varName", varName);
            nodeObject.put("type", "");
            //取实体类型
            if (type.equals(Constant.URI) && isStandardFormat) {
                String realValue = value;
                if (value.indexOf("/e-type/") != -1) {
                    realValue = value.split("/e-type/")[1];
                }
                if (realValue.indexOf("/") != 0) {
                    nodeObject.put("type", realValue.split("/")[0]);
                }
            }
            //ID
            nodeObject.put("id", id);

            //显示值  可能会被截图
            nodeObject.put("label", label);
            //节点为 实体或属性
            nodeObject.put("types", type.equals(Constant.URI) ? Constant.URI : Constant.LITERAL);

            //自定义信息
            JSONObject properties = new JSONObject();
            /**
             * datatype 常见数据类型包括
             * <http://www.w3.org/2001/XMLSchema#integer>（整数类型），
             * <http://www.w3.org/2001/XMLSchema#decimal>（定点类型），xsd:double（双精度浮点类型），
             * <http://www.w3.org/2001/XMLSchema#string>（字符串类型），
             * <http://www.w3.org/2001/XMLSchema#boolean>（布尔类型），
             * <http://www.w3.org/2001/XMLSchema#dateTime>（日期时间类型）
             */
            properties.put("datatype", datatype);
            properties.put("id", id);
            //节点为 实体或属性
            properties.put("type", type.equals(Constant.URI) ? Constant.URI : Constant.LITERAL);
            //实际值
            properties.put("value", value);
            //页面查找路径  查找环 等情况使用
            properties.put("label", label);
            nodeObject.put("properties", properties);
            nodes.add(nodeObject);
            return nodes;
        }
        return nodes;
    }

    //添加关系
    public static JSONArray addLinks(JSONArray links, JSONObject havLinks, String source, String target, String label) {
        JSONObject link = new JSONObject();
        link.put("source", source);
        link.put("target", target);
        link.put("label", label);
        if (checkLinkIsExist(link, havLinks) == false) {
            link.putAll(LinkStyle(link));
            links.add(link);
        }
        return links;
    }

    /**
     * 修改gStore数据库中的Uri实体
     *
     * @param oldUriData 原Uri 天龙八部
     * @param newUriData 新Uri 西游记
     * @param gc         连接
     * @param dbname     数据库
     * @param LiteralMap 修改Uri中指定的属性值
     * @throws Exception
     */
    public static Map updateRdfUriData(GStoreConnector gc, String dbname, String oldUriData, String newUriData, Map<String, Object> LiteralMap) throws Exception {
        String sparql = "select * where {  {?a ?b <%s>}union {<%s> ?c ?d.} }";
        String spasql = String.format(sparql, oldUriData, oldUriData);
        JSONObject result1 = queryData(gc, dbname, spasql);

        JSONArray bindsDataClmc = result1.getJSONObject("results").getJSONArray("bindings");
        List<String> delSparqlList = new ArrayList<>();
        List<String> addSparqlList = new ArrayList<>();

        bindsDataClmc.forEach(item -> {
            JSONObject items = (JSONObject) item;
            String baseUri = "<%s> <%s> <%s>";
            String baseLiteral = "<%s> <%s> \"%s\"^^<%s>";
            if (items.getJSONObject("a") != null) {
                delSparqlList.add(String.format(baseUri, items.getJSONObject("a").getString("value"), items.getJSONObject("b").getString("value"), oldUriData));
                addSparqlList.add(String.format(baseUri, items.getJSONObject("a").getString("value"), items.getJSONObject("b").getString("value"), newUriData));
            } else if (items.getJSONObject("c") != null) {
                String objVal = items.getJSONObject("d").getString("value");
                boolean isUri = true;
                String datatype = "";
                String oldObjVal = items.getJSONObject("d").getString("value");
                if (!items.getJSONObject("d").getString("type").equals(Constant.URI)) {
                    datatype = items.getJSONObject("d").getString("datatype");
                    isUri = false;
                    //自定义修改属性值
                    if (LiteralMap.get(items.getJSONObject("c").getString("value")) != null) {
                        objVal = LiteralMap.get(items.getJSONObject("c").getString("value")) + "";
                    } else {
                        objVal = items.getJSONObject("d").getString("value");
                    }
                }
                delSparqlList.add(String.format(isUri ? baseUri : baseLiteral, oldUriData, items.getJSONObject("c").getString("value"), oldObjVal, datatype));
                addSparqlList.add(String.format(isUri ? baseUri : baseLiteral, newUriData, items.getJSONObject("c").getString("value"), objVal, datatype));
            }
        });


        String delSparql = "delete data { %s }";
        String addSparql = "insert data { %s }";

        Map resultMap = new HashMap();
        if (delSparqlList.size() > 0) {
            resultMap.put("delSparql", String.format(delSparql, StringUtil.join(delSparqlList, ".")));
        }
        if (addSparqlList.size() > 0) {
            resultMap.put("addSparql", String.format(addSparql, StringUtil.join(addSparqlList, ".")));
        }
        return resultMap;
    }

    /**
     * @param query
     * @return
     */
    private static List<Element> getElementList(Query query) {
        Element element = query.getQueryPattern();
        ElementGroup elementGroup = (ElementGroup) element;
        List<Element> elementList = elementGroup.getElements();
        List<Element> result = new ArrayList<>();
        for (Element ele : elementList) {
            if (ele instanceof ElementPathBlock) {
                result.add(ele);
            } else if (ele instanceof ElementOptional) {
                // 查询中可选
                ElementOptional elementOptional = (ElementOptional) ele;
                ElementGroup group = (ElementGroup) elementOptional.getOptionalElement();
                List<Element> list = group.getElements();
                for (Element optionEle : list) {
                    result.add(optionEle);
                }
            } else if (ele instanceof ElementUnion) {
                ElementUnion elementUnion = (ElementUnion) ele;
                List<Element> list = elementUnion.getElements();
                for (Element unionEle : list) {
                    result.add(unionEle);
                }
            }
        }
        return result;
    }

    /**
     * 判断主谓宾是否重复
     *
     * @param duplicate
     * @param key
     * @return
     */
    public static JSONObject checkSPO(JSONObject duplicate, String key) {

        String keyVal = duplicate.getString(key);
        if (StringUtil.isNotBlank(keyVal)) {
            return duplicate;
        } else {
            duplicate.put(key, "1");
            return null;
        }
    }

    /**
     * 判断节点是否已经存在
     *
     * @param nodes
     * @param name
     * @param types
     * @return
     */
    public static JSONObject checkNodeIsExist(String name, String types, JSONObject nodes) {
        StringBuffer key = new StringBuffer(name).append("_").append(types);
        String keyVal = nodes.getString(key.toString());
        if (StringUtil.isNotBlank(keyVal) && types.equals(Constant.URI)) {
            return nodes;
        } else {
            nodes.put(key.toString(), "1");
            return null;
        }
    }

    /**
     * 判断关系是否已经存在
     *
     * @param link
     * @param links
     * @return
     */
    public static boolean checkLinkIsExist(JSONObject link, JSONObject links) {

        StringBuffer key = new StringBuffer(link.getString("source")).append("_").append(link.getString("label")).append("_").append(link.getString("target"));
        String keyVal = links.getString(key.toString());
        if (StringUtil.isNotBlank(keyVal)) {
            return true;
        } else {
            links.put(key.toString(), "1");
            return false;
        }
    }


    /**
     * 判断节点是否存在
     * 建议不要使用
     *
     * @param name
     * @param types
     * @param nodes
     * @return
     */
    public static JSONObject checkIsExist(String name, String types, JSONArray nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject jsonObject = nodes.getJSONObject(i);
            if (jsonObject.containsKey("id") && jsonObject.getString("id").equals(name) && jsonObject.containsKey("types") && jsonObject.getString("types").equals(types)) {
                return jsonObject;
            }
        }
        return null;
    }

    /**
     * 建议不要使用
     *
     * @param link
     * @param links
     * @return
     */
    public static boolean checkLink(JSONObject link, JSONArray links) {
        for (int i = 0; i < links.size(); i++) {
            JSONObject tempLink = links.getJSONObject(i);
            if (tempLink.getString("target").equals(link.getString("target")) && tempLink
                    .getString("source").equals(link.getString("source")) && tempLink
                    .getString("label").equals(link.getString("label"))) {
                return true;
            }

        }
        return false;
    }


    /**
     * 关系的效果
     *
     * @param link
     * @return
     */
    public static JSONObject LinkStyle(JSONObject link) {
        link.put("id", UUID.randomUUID());
        //  link.put("alpha", 0.8);
        //  link.put("showArrow", true);
        //  link.put("lineType", "direct");
        //  link.put("colorType", "s");
        // link.put("colorType", "defined");
        //  link.put("color", "250,250,250");
        //  link.put("font", "10px 微软雅黑");
        //   link.put("lineWidth", 1);
        return link;
    }


    public static JSONArray getSeniorPaths(String result) {
        if (StringUtil.isNotEmpty(result) && result.contains("paths")) {
            JSONObject gStoreJson = JSONObject.parseObject(result);
            if (gStoreJson.getInteger("StatusCode") == 0) {
                String key = gStoreJson.getJSONObject("head").getJSONArray("vars").getString(0);
                JSONArray bindsData = gStoreJson.getJSONObject("results").getJSONArray("bindings");
                System.err.println(bindsData);
                AtomicReference<JSONArray> jsonArray = new AtomicReference<>(new JSONArray());
                if (bindsData.size() > 0) {
                    bindsData.forEach(item -> {
                        JSONObject j = (JSONObject) item;
                        if (!j.isEmpty()) {
                            jsonArray.set(j.getJSONObject(key).getJSONObject("value").getJSONArray("paths"));
                        }
                    });

                }
                return jsonArray.get();
//                return bindsData.getJSONObject(0).getJSONObject(key).getJSONObject("value").getJSONArray("paths");
            }
        }
        return new JSONArray();
    }

    public static JSONObject getSubBindJson(JSONArray bindsArray, int index) {
        return bindsArray.getJSONObject(index);
    }

    public static JSONArray getSubBindJsonArray(JSONObject obj, String key) {
        return obj.getJSONArray(key);
    }
}
