package com.jwlz.sjjc.zstp.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jwlz.sjjc.zstp.common.Constant;

import java.util.HashMap;
import java.util.Map;

public class GStoreConnector91 extends GStoreConnector {

    private static final int timeout_30s = 30000;
    private static final int timeout_120s = 120000;

    public GStoreConnector91(String _ip, int _port, String userName, String password, String accessType) {
        this.serverIP = _ip;
        this.serverPort = _port;
        this.userName = userName;
        this.password = password;
        this.accessType = accessType;
        if (Constant.GRPC.equals(accessType)) {
            this.url = "http://" + this.serverIP + ":" + this.serverPort + "/grpc/api";
        } else {
            this.url = "http://" + this.serverIP + ":" + this.serverPort;
        }
        this.version = "0.9";
    }

    public String login() {
        JSONObject json = new JSONObject();
        json.put("operation", "login");
        json.put("username", userName);
        json.put("password", password);
        String data = json.toString();
        String msg = HttpUtils.sendPost(url, data, timeout_30s);
        return msg;
    }

    public String clearUserPrivileges(String opusername) {
        JSONObject json = new JSONObject();
        json.put("operation", "userprivilegemanage");
        json.put("username", userName);
        json.put("password", password);
        json.put("type", "3");
        json.put("op_username", opusername);
        String data = json.toString();
        String msg = HttpUtils.sendPost(url, data, timeout_30s);
        return msg;
    }

    public String userPrivilegesManage(String opusername, String opType, String privileges, String db_name) {
        JSONObject json = new JSONObject();
        json.put("operation", "userprivilegemanage");
        json.put("username", userName);
        json.put("password", password);
        json.put("type", opType);
        json.put("op_username", opusername);
        json.put("privileges", privileges);
        json.put("db_name", db_name);
        String data = json.toString();
        String msg = HttpUtils.sendPost(url, data, timeout_30s);
        return msg;
    }

    @Override
    public String userpassword(String op_password) {
        throw new RuntimeException("当前版本没有此功能");
    }

    public String testConnect() {
        JSONObject json = new JSONObject();
        json.put("operation", "testConnect");
        json.put("username", userName);
        json.put("password", password);
        String data = json.toString();
        String msg = HttpUtils.sendPost(url, data, timeout_30s);
        return msg;
    }

    public String load(String db_name) {
        JSONObject json = new JSONObject();
        json.put("operation", "load");
        json.put("username", userName);
        json.put("password", password);
        json.put("db_name", db_name);
        String data = json.toString();
        String msg = HttpUtils.sendPost(url, data, timeout_30s);
        return msg;
    }

    public String unload(String db_name) {
        JSONObject json = new JSONObject();
        json.put("operation", "unload");
        json.put("username", userName);
        json.put("password", password);
        json.put("db_name", db_name);
        String data = json.toString();
        String msg = HttpUtils.sendPost(url, data, timeout_30s);
        return msg;
    }

    public String build(String db_name, String rdf_file_path) {
        JSONObject json = new JSONObject();
        json.put("operation", "build");
        json.put("username", userName);
        json.put("password", password);
        json.put("db_name", db_name);
        json.put("db_path", rdf_file_path);
        String data = json.toString();
        String msg = HttpUtils.sendPost(url, data, timeout_30s);
        return msg;
    }

    public String drop(String db_name, boolean is_backup) {
        JSONObject json = new JSONObject();
        json.put("operation", "drop");
        json.put("username", userName);
        json.put("password", password);
        json.put("db_name", db_name);
        json.put("is_backup", is_backup + "");
        String data = json.toString();
        String msg = HttpUtils.sendPost(url, data, timeout_30s);
        return msg;
    }

    public String query(String db_name, String sparql) {
        Map<String, String> map = new HashMap<>();
        map.put("operation", "query");
        map.put("username", userName);
        map.put("password", password);
        map.put("db_name", db_name);
        map.put("format", "json");
        map.put("sparql", sparql);
        String msg = HttpUtils.sendPost(url, JSON.toJSONString(map), timeout_120s);
        return msg;
    }

//    public void query(String _username, String _password, String _db_name, String _sparql, String _filename) {
//        String cmd = "?operation=query&username=" + _username + "&password=" + _password + "&db_name=" + _db_name + "&format=json&sparql=" + _sparql;
//        HttpUtils.sendGet(url, cmd, _filename);
//        return;
//    }

    //show all databases
    public String show() {
        Map<String, String> map = new HashMap<>();
        map.put("operation", "show");
        map.put("username", userName);
        map.put("password", password);
        String msg = HttpUtils.sendPost(url, JSON.toJSONString(map), timeout_30s);
        JSONObject json = JSONObject.parseObject(msg);
        if (json.getInteger("StatusCode") == 0) {
            String result = json.getString("ResponseBody");
            JSONArray array = JSONArray.parseArray(result);
            json.put("list", array);
        }
        return json.toString();
    }

    public String user(String type, String username2, String addition) {
        Map<String, String> map = new HashMap<>();
        map.put("operation", "user");
        map.put("username1", userName);
        map.put("password1", password);
        map.put("type", type);
        map.put("username2", username2);
        map.put("addition", addition);
        String msg = HttpUtils.sendPost(url, JSON.toJSONString(map), timeout_30s);
        return msg;
    }

    public String usermanage(String type, String username2, String password2) {
        JSONObject postdata = new JSONObject();
        postdata.put("operation", "usermanage");
        postdata.put("type", type);
        postdata.put("username", this.userName);
        postdata.put("password", this.password);
        postdata.put("op_username", username2);
        postdata.put("op_password", password2);
        String res = HttpUtils.sendPost(url, postdata.toString(), timeout_30s);
        return res;
    }

    public String showUser() {
        JSONObject postdata = new JSONObject();
        postdata.put("operation", "showuser");
        postdata.put("username", this.userName);
        postdata.put("password", this.password);
        String res = HttpUtils.sendPost(url, postdata.toString(), timeout_30s);
        return res;
    }

    public String monitor(String db_name) {
        Map<String, String> map = new HashMap<>();
        map.put("operation", "monitor");
        map.put("username", userName);
        map.put("password", password);
        map.put("db_name", db_name);
        String msg = HttpUtils.sendPost(url, JSON.toJSONString(map), timeout_30s);

        JSONObject json = JSONObject.parseObject(msg);
        if (json.getInteger("StatusCode") == 0) {
            if(null != json.get("built_time")) {
                json.put("builtTime", json.get("built_time"));
            }
            if(null != json.get("triple num")) {
                json.put("tripleNum", json.get("triple num"));
            }
            if(null != json.get("entity num")) {
                json.put("entityNum", json.get("entity num"));
            }
            if(null != json.get("literal num")) {
                json.put("literalNum", json.get("literal num"));
            }
            if(null != json.get("subject num")) {
                json.put("subjectNum", json.get("subject num"));
            }
            if(null != json.get("predicate num")) {
                json.put("predicateNum", json.get("predicate num"));
            }
            if(null != json.get("connection num")) {
                json.put("connectionNum", json.get("connection num"));
            }
        }
        return json.toString();
    }

    public String checkpoint(String db_name) {
        String cmd = "?operation=checkpoint&db_name=" + db_name + "&username=" + userName + "&password=" + password;
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
        return msg;
    }

    //备份到指定路径，当path为null时，默认备份到backups目录下
    public String backup(String db_name, String path) {
        String cmd = "?operation=backup&db_name=" + db_name + "&username=" + this.userName + "&password=" + this.password + "&backup_path=" + path;
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
        return msg;
    }

    //恢复数据库(包括已经损坏的、已经删除的，和正常的数据库)
    public String restore(String db_name, String path) {
        String cmd = "?operation=restore&db_name=" + db_name + "&username=" + this.userName + "&password=" + this.password + "&backup_path=" + path;
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
        return msg;

    }

    //导出数据库
    public String exportDB(String db_name, String dir_path) {
        String res = "";
        JSONObject postdata = new JSONObject();
        postdata.put("operation", "export");
        postdata.put("db_name", db_name);
        postdata.put("db_path", dir_path);
        postdata.put("username", this.userName);
        postdata.put("password", this.password);
//        String strUrl = "export";
//        String strPost = "{\"db_name\": \"" + db_name + "\", \"db_path\": \"" + dir_path + "\", \"username\": \"" + this.userName + "\", \"password\": \"" + this.password + "\"}";
//        res = HttpUtils.sendPost(url, strUrl, strPost, timeout_30s);
        res = HttpUtils.sendPost(url, postdata.toString(), timeout_30s);
        return res;
    }

    //事务管理
    public String txnlog(String keyword, String page, String limit) {
        String res = "";
        JSONObject postdata = new JSONObject();
        postdata.put("operation", "txnlog");
        postdata.put("username", this.userName);
        postdata.put("password", this.password);
        postdata.put("pageNo", page);
        postdata.put("pageSize", Integer.valueOf(limit));
        res = HttpUtils.sendPost(url, postdata.toString(), timeout_30s);
        return res;
    }

    //日志查询
    public String querylog(String page, String limit, String date) {
        String res = "";
        JSONObject postdata = new JSONObject();
        postdata.put("operation", "querylog");
        postdata.put("date", date);
        postdata.put("pageNo", page);
        postdata.put("pageSize", Integer.valueOf(limit));
        postdata.put("username", this.userName);
        postdata.put("password", this.password);
        res = HttpUtils.sendPost(url, postdata.toString(), timeout_30s);
        return res;
    }

    // 操作日志
    public String oplog(String page, String limit, String date) {
        String res = "";
        JSONObject postdata = new JSONObject();
        postdata.put("operation", "accesslog");
        postdata.put("date", date);
        postdata.put("pageNo", page);
        postdata.put("pageSize", Integer.valueOf(limit));
        postdata.put("username", this.userName);
        postdata.put("password", this.password);
        res = HttpUtils.sendPost(url, postdata.toString(), timeout_30s);
        return res;
    }

    /**
     * 提交事务
     *
     * @param db_name
     * @param tid
     * @return
     */
    public String commit(String db_name, String tid) {
//        String strUrl = "?operation=commit" + "&db_name=" + db_name + "&tid=" + tid + "&username=" + this.userName + "&password=" + this.password;
//        String res = HttpUtils.sendGet(url, strUrl, timeout_30s);
        JSONObject postdata = new JSONObject();
        postdata.put("operation", "commit");
        postdata.put("db_name", db_name);
        postdata.put("tid", tid);
        postdata.put("username", userName);
        postdata.put("password", password);
        String res = HttpUtils.sendPost(url, postdata.toString(), timeout_30s);
        return res;
    }

    public String ipmanageSelect() {
        String res = "";
        JSONObject postdata = new JSONObject();
        postdata.put("operation", "ipmanage");
        postdata.put("type", "1");
        postdata.put("username", this.userName);
        postdata.put("password", this.password);
        res = HttpUtils.sendPost(url, postdata.toString(), timeout_30s);
        return res;
    }

    public String ipmanageEdit(String ips, String ipType) {
        String res = "";
        JSONObject postdata = new JSONObject();
        postdata.put("operation", "ipmanage");
        postdata.put("type", "2");
        postdata.put("ip_type", ipType);
        postdata.put("ips", ips);
        postdata.put("username", this.userName);
        postdata.put("password", this.password);
        res = HttpUtils.sendPost(url, postdata.toString(), timeout_30s);
        return res;
    }

    /**
     * 回滚事务
     *
     * @param db_name
     * @param tid
     * @return
     */
    public String rollback(String db_name, String tid) {
//        String strUrl = "?operation=rollback" + "&db_name=" + db_name + "&tid=" + tid + "&username=" + this.userName + "&password=" + this.password;
//        String res = HttpUtils.sendGet(url, strUrl, timeout_30s);
        JSONObject postdata = new JSONObject();
        postdata.put("operation", "rollback");
        postdata.put("db_name", db_name);
        postdata.put("tid", tid);
        postdata.put("username", userName);
        postdata.put("password", password);
        String res = HttpUtils.sendPost(url, postdata.toString(), timeout_30s);
        return res;
    }

    public String batchInsert(String db_name, String file) {
        Map<String, String> map = new HashMap<>();
        map.put("operation", "batchInsert");
        map.put("username", userName);
        map.put("password", password);
        map.put("db_name", db_name);
        map.put("file", file);
        String msg = HttpUtils.sendPost(url, JSON.toJSONString(map), timeout_30s);
        return msg;
    }

    public String begin(String db_name) {
        Map<String, String> map = new HashMap<>();
        map.put("operation", "begin");
        map.put("username", userName);
        map.put("password", password);
        map.put("db_name", db_name);
        map.put("isolevel", "1");
        String msg = HttpUtils.sendPost(url, JSON.toJSONString(map), timeout_30s);
        return msg;
    }

    public String tquery(String db_name, String sparql, String tid) {
        Map<String, String> map = new HashMap<>();
        map.put("operation", "tquery");
        map.put("username", userName);
        map.put("password", password);
        map.put("db_name", db_name);
        map.put("sparql", sparql);
        map.put("tid", tid);
        String msg = HttpUtils.sendPost(url, JSON.toJSONString(map), timeout_30s);
        return msg;
    }

    @Override
    public String funlist(String fun_name, String fun_status) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String funcudb(String type, JSONObject funInfo) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String funreview(JSONObject funInfo) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String backuppath(String db_name) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String getCoreVersion() {
        Map<String, String> map = new HashMap<>();
        map.put("operation", "getCoreVersion");
        map.put("username", userName);
        map.put("password", password);
        String msg = HttpUtils.sendPost(url, JSON.toJSONString(map), timeout_30s);
        return msg;
    }

    public String load(String db_name, Boolean csr) {
        JSONObject json = new JSONObject();
        json.put("operation", "load");
        json.put("username", userName);
        json.put("password", password);
        json.put("db_name", db_name);
        json.put("load_csr", csr.toString());
        String data = json.toString();
        String msg = HttpUtils.sendPost(url, data, timeout_30s);
        return msg;
    }
}

