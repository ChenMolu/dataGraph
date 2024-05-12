package com.jwlz.sjjc.zstp.utils.pkumod.result;



import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author wj
 */
public class Result implements Serializable {

    private int statusCode;

    private String statusMsg;

    private List<String> vars;

    private List<Record> records;

    public Result() {
        vars = new ArrayList<>();
        records = new ArrayList<>();
    }

    public static Result instance() {
        return new Result();
    }

    public boolean success() {
        return statusCode == 0;
    }

    public String statusMsg() {
        return statusMsg;
    }

    public List<String> vars() {
        return vars;
    }

    /**
     * 返回第一条数据
     *
     * @return
     */
    public Record single() {
        if (!records.isEmpty()) {
            return records.get(0);
        } else {
            return null;
        }
    }

    /**
     * 返回列表 适用于查询
     *
     * @return
     */
    public List<Record> list() {
        return records;
    }

    public String listToJSONString() {
        return JSON.toJSONString(records);
    }

    public void consume() {
        //do nothing
    }

    public Stream<Record> stream() {
        Spliterator<Record> spliterator = Spliterators.spliteratorUnknownSize(records.iterator(), 1040);
        return StreamSupport.stream(spliterator, false);
    }

    public Result build(String str) throws Exception {
        JSONObject json = JSONObject.parseObject(str);
        if (json == null) {
            throw new Exception("parse json string error.");
        }
        if (json.containsKey("StatusCode")) {
            this.statusCode = json.getInteger("StatusCode");
        }
        if (json.containsKey("StatusMsg")) {
            this.statusMsg = json.getString("StatusMsg");
        }
        String[] vars = null;
        if (json.containsKey("head")) {
            JSONObject headNode = json.getJSONObject("head");
            if (headNode.containsKey("vars")) {
                JSONArray varNode = headNode.getJSONArray("vars");
                vars = new String[varNode.size()];
                for (int i = 0; i < varNode.size(); i++) {
                    vars[i] = varNode.getString(i);
                    this.vars.add(vars[i]);
                }
            }
        }
        if (json.containsKey("results") && vars != null) {
            JSONObject dataNode = json.getJSONObject("results");
            if (dataNode.containsKey("bindings")) {
                JSONArray bindingsNode = dataNode.getJSONArray("bindings");

                for (int i = 0; i < bindingsNode.size(); i++) {
                    JSONObject item = bindingsNode.getJSONObject(i);
                    Value[] values = new Value[vars.length];
                    for (int j = 0; j < vars.length; j++) {
                        if (item.containsKey(vars[j])) {
                            values[j] = JSON.parseObject(String.valueOf(item.getJSONObject(vars[j])), Value.class);
                        }
                    }
                    Record record = new Record(vars, values);
                    this.records.add(record);
                }
            }
        }
        return this;
    }
}
