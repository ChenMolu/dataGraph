package com.jwlz.sjjc.zstp.utils;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

public class GStoreConnector8 extends GStoreConnector {

    private static final int timeout_30s = 30000;
    private static final int timeout_120s = 120000;

    public GStoreConnector8(String _ip, int _port, String userName, String password) {
        this.serverIP = _ip;
        this.serverPort = _port;
        this.userName = userName;
        this.password = password;
        this.url = url = "http://" + this.serverIP + ":" + this.serverPort;
        this.version = "0.8";
    }

    public String load(String _db_name) {
        String cmd = "?operation=load&db_name=" + _db_name + "&username=" + userName + "&password=" + password;
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
        return msg;
    }

    public String unload(String _db_name) {
        String cmd = "?operation=unload&db_name=" + _db_name + "&username=" + userName + "&password=" + password;
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
        return msg;
    }

    public String build(String _db_name, String _rdf_file_path) {
        String cmd = "?operation=build&db_name=" + _db_name + "&ds_path=" + _rdf_file_path + "&username=" + userName + "&password=" + password;
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
        return msg;
    }

    public String drop(String db_name, boolean is_backup) {
        String cmd = "";
        if (is_backup) {
            cmd = "?operation=drop&db_name=" + db_name + "&username=" + userName +
                    "&password=" + password + "&is_backup=true";
        } else {
            cmd = "?operation=drop&db_name=" + db_name + "&username=" + userName +
                    "&password=" + password + "&is_backup=false";
        }
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
        return msg;
    }


    public String getCoreVersion() {
        String res = "";
        String strUrl = "getCoreVersion";
        String strPost = "{\"username\": \"" + userName + "\", \"password\": \"" + password + "\"}";
        res = HttpUtils.sendPost(url, strUrl, strPost, timeout_30s);
        return res;
    }

    public String getAPIVersion() {
        String res = "";
        String strUrl = "getAPIVersion";
        String strPost = "{\"username\": \"" + userName + "\", \"password\": \"" + password + "\"}";
        res = HttpUtils.sendPost(url, strUrl, strPost, timeout_30s);
        return res;
    }

    public String initVersion() {
        String res = "";
        String strUrl = "initVersion";
        String strPost = "{\"username\": \"" + userName + "\", \"password\": \"" + password + "\"}";
        res = HttpUtils.sendPost(url, strUrl, strPost, timeout_30s);
        return res;
    }

    public String query(String _db_name, String _sparql) {
        String cmd = "?operation=query&username=" + userName + "&password=" + password + "&db_name=" + _db_name + "&format=json&sparql=" + _sparql;
        String msg = HttpUtils.sendGet(url, cmd, timeout_120s);
        return msg;
    }

    //show all databases
    public String show() {
        String cmd = "?operation=show&username=" + userName + "&password=" + password;
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
        JSONObject json = JSONObject.parseObject(msg);
        if (json.getInteger("StatusCode") == 0) {
            String result = json.getString("ResponseBody");
            JSONArray array = JSONArray.parseArray(result);
            json.put("list", array);
        }
        return json.toString();
    }


    public String user(String type, String username2, String addition) {
        String res = "";
        String strUrl = "user";
        String strPost = "{\"type\": \"" + type + "\", \"username1\": \"" + this.userName + "\", \"password1\": \"" + this.password + "\", \"username2\": \"" + username2 + "\", \"addition\": \"" + addition + "\"}";
        res = HttpUtils.sendPost(url, strUrl, strPost, timeout_30s);
        return res;
    }

    @Override
    public String usermanage(String type, String username2, String password2) {
        throw new RuntimeException("当前版本没有此功能");
    }

    public String showUser() {
        String res = "";
        String strUrl = "showUser";
        String strPost = "{\"username\": \"" + this.userName + "\", \"password\": \"" + this.password + "\"}";
        res = HttpUtils.sendPost(url, strUrl, strPost, timeout_30s);
        return res;
    }

    public String monitor(String db_name) {
        String cmd = "?operation=monitor&db_name=" + db_name + "&username=" + userName + "&password=" + password;
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
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
        String cmd = "?operation=backup&db_name=" + db_name + "&username=" + this.userName + "&password=" + this.password + "&path=" + path;
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
        return msg;
    }

    //恢复数据库(包括已经损坏的、已经删除的，和正常的数据库)
    public String restore(String db_name, String path) {
        String cmd = "?operation=restore&db_name=" + db_name + "&username=" + this.userName + "&password=" + this.password + "&path=" + path;
        String msg = HttpUtils.sendGet(url, cmd, timeout_30s);
        return msg;

    }

    //导出数据库
    public String exportDB(String db_name, String dir_path) {
        String res = "";
        String strUrl = "export";
        String strPost = "{\"db_name\": \"" + db_name + "\", \"ds_path\": \"" + dir_path + "\", \"username\": \"" + this.userName + "\", \"password\": \"" + this.password + "\"}";
        res = HttpUtils.sendPost(url, strUrl, strPost, timeout_30s);
        return res;
    }

    @Override
    public String txnlog(String keyword, String page, String limit) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String querylog(String page, String limit, String date) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String oplog(String page, String limit, String date) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String commit(String db_name, String tid) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String ipmanageSelect() {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String ipmanageEdit(String ips, String ipType) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String rollback(String db_name, String tid) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String begin(String db_name) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String tquery(String db_name, String sparql, String tid) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String login() {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String clearUserPrivileges(String opusername) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String userPrivilegesManage(String opusername, String opType, String privileges, String db_name) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String userpassword(String op_password) {
        throw new RuntimeException("当前版本没有此功能");
    }

    @Override
    public String testConnect() {
        throw new RuntimeException("当前版本没有此功能");
    }

    public String batchInsert(String db_name, String file) {
        throw new RuntimeException("当前版本没有此功能");
    }

    public String load(String db_name, Boolean scr) {
        throw new RuntimeException("当前版本没有此功能");
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
}

