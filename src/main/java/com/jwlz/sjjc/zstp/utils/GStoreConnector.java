package com.jwlz.sjjc.zstp.utils;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.io.UnsupportedEncodingException;

@Data
public abstract class GStoreConnector {

    protected String serverIP;
    protected int serverPort;
    protected String userName;
    protected String password;
    protected String url;
    protected String accessType;
    protected String version;

    public static GStoreConnector getGStoreConnector(String version, String ip, int port, String username, String password, String accessType) {
        GStoreConnector gc = null;
        if ("0.8".equals(version) ) {
            gc = new GStoreConnector8(ip, Integer.valueOf(port), username, password);
        } else if("0.9.1".equals(version)){
            gc = new GStoreConnector91(ip, Integer.valueOf(port), username, password, accessType);
        } else {
            gc = new GStoreConnector10(ip, Integer.valueOf(port), username, password, accessType);
        }
        return gc;
    }

    public abstract  String login();

    public abstract String clearUserPrivileges(String opusername);

    public abstract String userPrivilegesManage(String opusername, String opType, String privileges, String db_name);

    public abstract String testConnect();
    //用户修改密码 gstore1.0新增
    public abstract String userpassword(String op_password);

    public abstract String load(String _db_name);

    public abstract String load(String db_name, Boolean csr);

    public abstract String unload(String _db_name);

    public abstract String build(String _db_name, String _rdf_file_path);

    public abstract String drop(String db_name, boolean is_backup);

    public abstract String query(String _db_name, String _sparql);

//    public abstract void query(String _username, String _password, String _db_name, String _sparql, String _filename);

    public abstract String show();

    public abstract String user(String type, String username2, String addition);

    public abstract String usermanage(String type, String username2, String password2);

    public abstract String showUser();

    public abstract String monitor(String db_name);

    public abstract String checkpoint(String db_name);

    //备份到指定路径，当path为null时，默认备份到backups目录下
    public abstract String backup(String db_name, String path);

    //恢复数据库(包括已经损坏的、已经删除的，和正常的数据库)
    public abstract String restore(String db_name, String path);

    //导出数据库
    public abstract String exportDB(String db_name, String dir_path);

    //事务管理
    public abstract String txnlog(String keyword, String page, String limit);

    //日志查询
    public abstract String querylog(String page, String limit, String date);

    // 操作日志
    public abstract String oplog(String page, String limit, String date);

    /**
     * 提交事务
     *
     * @param db_name
     * @param tid
     * @return
     */
    public abstract String commit(String db_name, String tid);

    public abstract String ipmanageSelect();

    public abstract String ipmanageEdit(String ips, String ipType);

    public abstract String batchInsert(String db_name, String file);
    /**
     * 回滚事务
     *
     * @param db_name
     * @param tid
     * @return
     */
    public abstract String rollback(String db_name, String tid);

    public abstract String begin(String db_name);

    public abstract String tquery(String db_name, String sparql, String tid);

    public abstract String funlist(String fun_name, String fun_status);

    public abstract String funcudb(String type, JSONObject funInfo);

    public abstract String funreview(JSONObject funInfo);

    public abstract String backuppath(String db_name);

    /**
     * 获取版本号
     * @return
     */
    public abstract String getCoreVersion();

    private static byte[] packageMsgData(String _msg) {
        //byte[] data_context = _msg.getBytes();
        byte[] data_context = null;
        try {
            data_context = _msg.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            data_context = _msg.getBytes();
        }
        int context_len = data_context.length + 1; // 1 byte for '\0' at the end of the context.
        int data_len = context_len + 4; // 4 byte for one int(data_len at the data's head).
        byte[] data = new byte[data_len];

        // padding head(context_len).
        byte[] head = GStoreConnector.intToByte4(context_len);
        for (int i = 0; i < 4; i++) {
            data[i] = head[i];
        }

        // padding context.
        for (int i = 0; i < data_context.length; i++) {
            data[i + 4] = data_context[i];
        }
        // in C, there should be '\0' as the terminator at the end of a char array. so we need add '\0' at the end of sending message.
        data[data_len - 1] = 0;
        return data;
    }

    // with Little Endian format.
    private static byte[] intToByte4(int _x) {
        byte[] ret = new byte[4];
        ret[0] = (byte) (_x);
        ret[1] = (byte) (_x >>> 8);
        ret[2] = (byte) (_x >>> 16);
        ret[3] = (byte) (_x >>> 24);
        return ret;
    }

    // with Little Endian format.
    private static int byte4ToInt(byte[] _b) {
        int byte0 = _b[0] & 0xFF, byte1 = _b[1] & 0xFF, byte2 = _b[2] & 0xFF, byte3 = _b[3] & 0xFF;
        int ret = (byte0) | (byte1 << 8) | (byte2 << 16) | (byte3 << 24);
        return ret;
    }

}
