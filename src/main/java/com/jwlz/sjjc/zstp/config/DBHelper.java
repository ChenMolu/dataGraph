//package com.hnu.zstp.config;
//
//import com.alibaba.druid.pool.DruidDataSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.lang.reflect.Field;
//import java.sql.*;
//import java.util.*;
//
//@Service
//public class DBHelper {
//
//    @Autowired
//    private DruidDataSource pool;
//
//    /**
//     * 链接获取
//     *
//     * @return
//     */
//    public Connection getConn() {
//        try {
//            return pool.getConnection();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//
//    /**
//     * 资源关闭
//     *
//     * @param stmt
//     * @param conn
//     */
//    public void close(Statement stmt, Connection conn) {
//        try {
//            if (stmt != null) {
//                stmt.close();
//            }
//            if (conn != null) {
//                conn.close();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void close(Connection conn) {
//        try {
//            if (conn != null) {
//                conn.close();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 释放资源
//     *
//     * @param conn
//     * @param st
//     * @param rs
//     */
//    public void closeResource(Connection conn, Statement st, ResultSet rs) {
//        closeResultSet(rs);
//        closeStatement(st);
//        closeConnection(conn);
//    }
//
//    /**
//     * 释放连接 Connection
//     *
//     * @param conn
//     */
//    public void closeConnection(Connection conn) {
//        if (conn != null) {
//            try {
//                conn.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        //等待垃圾回收
//        conn = null;
//    }
//
//    /**
//     * 释放语句执行者 Statement
//     *
//     * @param st
//     */
//    public void closeStatement(Statement st) {
//        if (st != null) {
//            try {
//                st.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        //等待垃圾回收
//        st = null;
//    }
//
//    /**
//     * 释放结果集 ResultSet
//     *
//     * @param rs
//     */
//    public void closeResultSet(ResultSet rs) {
//        if (rs != null) {
//            try {
//                rs.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        //等待垃圾回收
//        rs = null;
//    }
//
//    /**
//     * 封装通用的更新操作，对所有更新(INSERT,UPDATE，DELETE)有关的操作都能通过该方法实现
//     *
//     * @param sql
//     * @return
//     */
//    public boolean exeUpdate(Connection conn, String sql, Object... obj) throws Exception {
//        PreparedStatement ps = null;
//        try {
//            ps = conn.prepareStatement(sql);
//            for (int i = 0; i < obj.length; i++) {
//                ps.setObject(i + 1, obj[i]);
//            }
//            return ps.executeUpdate() > 0;
//        } finally {
//            close(ps, null);
//        }
//    }
//
//    public boolean exeUpdate(Connection conn, String sql, Vector<Object> sqlValues) throws Exception {
//        PreparedStatement ps = null;
//        try {
//            ps = conn.prepareStatement(sql);
//            ps.clearParameters();
//            for (int i = 0; i < sqlValues.size(); i++) {
//                ps.setObject(i + 1, sqlValues.get(i));
//            }
//            return ps.executeUpdate() > 0;
//        } finally {
//            close(ps, null);
//        }
//    }
//
//    /**
//     * 执行新增后，返回自增的ID值
//     *
//     * @param conn
//     * @param sql
//     * @param sqlValues
//     * @return
//     */
//    public Integer exeCreate(Connection conn, String sql, Vector<String> sqlValues) throws Exception {
//        PreparedStatement ps = null;
//        try {
//            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
//            ps.clearParameters();
//            for (int i = 0; i < sqlValues.size(); i++) {
//                ps.setObject(i + 1, sqlValues.get(i));
//            }
//            ps.executeUpdate();
//            ResultSet rs = ps.getGeneratedKeys();
//            int id = 0;
//            if (rs.next()) {
//                id = rs.getInt(1);
//            }
//            return id;
//        } finally {
//            close(ps, null);
//        }
//
//    }
//
//
//    public boolean exeUpdate(Connection conn, String sql) throws Exception {
//        PreparedStatement ps = null;
//        try {
//            ps = conn.prepareStatement(sql);
//            return ps.executeUpdate() > 0;
//        } finally {
//            close(ps, null);
//        }
//    }
//
//    public ResultSet queryList(Connection conn, String sql) throws Exception {
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//        ps = conn.prepareStatement(sql);
//        rs = ps.executeQuery();
//        return rs;
//    }
//
//    public ResultSet queryList(Connection conn, String sql, Vector<Object> sqlValues) throws Exception {
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//        ps = conn.prepareStatement(sql);
//        if (sqlValues != null) {
//            for (int i = 0; i < sqlValues.size(); i++) {
//                ps.setObject(i + 1, sqlValues.get(i));
//            }
//        }
//        rs = ps.executeQuery();
//        return rs;
//    }
//
//    /**
//     * 技术参数: 泛型，集合框架，反射，JDBC 封装通用查询多条及操作
//     *
//     * @param t
//     * @param sql
//     * @param params
//     * @return
//     */
//    public <T> List<T> queryList(Class<T> t, String sql, Object... params) throws Exception {
//        List<T> list = new ArrayList<>();
//        T obj = null;
//        Connection conn = null;
//        PreparedStatement ps = null;
//        conn = getConn();
//        ps = conn.prepareStatement(sql);
//        if (params != null) {
//            for (int i = 0; i < params.length; i++) {
//                ps.setObject(i + 1, params[i]);
//            }
//        }
//        ResultSet rs = ps.executeQuery();
//        // 获取插叙结果集中的元数据(获取列类型，数量以及长度等信息)
//        ResultSetMetaData rsmd = rs.getMetaData();
//        // 声明一个map集合，用于临时存储查询到的一条数据（key：列名；value：列值）
//        Map<String, Object> map = new HashMap<>();
//        // 遍历结果集
//        while (rs.next()) {
//            // 防止缓存上一条数据
//            map.clear();
//            // 遍历所有的列
//            for (int i = 0; i < rsmd.getColumnCount(); i++) {
//                // 获取列名
//                String cname = rsmd.getColumnLabel(i + 1);
//                //获取列类型的int表示形式，以及列类型名称
////					System.out.println("列类型:"+rsmd.getColumnType(i + 1)+"----"+rsmd.getColumnTypeName(i+1));
//                // 获取列值
//                Object value = rs.getObject(cname);
//                // 将列明与列值存储到map中
//                map.put(cname, value);
//            }
//            // 利用反射将map中的数据注入到Java对象中，并将对象存入集合
//            if (!map.isEmpty()) {
//                // 获取map集合键集(列名集合)
//                Set<String> columnNames = map.keySet();
//                // 创建对象
//                obj = t.newInstance();//new Student() //java.lang.Object
//                for (String column : columnNames) {
//                    // 根据键获取值
//                    Object value = map.get(column);
//                    //当数据对象不为空时，才注入数据到属性中
//                    if (Objects.nonNull(value)) {
//                        // 获取属性对象
//                        Field f = t.getDeclaredField(column);
//                        // 设置属性为可访问状态
//                        f.setAccessible(true);
//                        // 为属性设置
//                        f.set(obj, value);
//                    }
//                }
//                list.add(obj);
//            }
//        }
//        closeResource(conn, null, rs);
//        return list;
//    }
//
//    /**
//     * 封装查询单个对象的方法
//     *
//     * @param t
//     * @param sql
//     * @param params
//     * @return
//     */
//    public <T> T queryOne(Class<T> t, String sql, Object... params) throws Exception {
//        T obj = null;
//        Connection conn = null;
//        PreparedStatement ps = null;
//        conn = getConn();
//        ps = conn.prepareStatement(sql);
//        for (int i = 0; i < params.length; i++) {
//            ps.setObject(i + 1, params[i]);
//        }
//        ResultSet rs = ps.executeQuery();
//        ResultSetMetaData rsmd = rs.getMetaData();
//        //ORM操作（对象关系映射）
//        if (rs.next()) {
//            // 创建一个指定类型的实例对象(必须包含默认构造器)
//            obj = t.newInstance();
//            for (int i = 0; i < rsmd.getColumnCount(); i++) {
//                //获取指定列的列名称
//                String cname = rsmd.getColumnLabel(i + 1);
//                //获取列值
//                Object value = rs.getObject(cname);
//                if (Objects.nonNull(value)) {
//                    //根据列名称获取Java类的属性名(要求表中的列名称必须与类中的属性名保持一致)
//                    Field field = t.getDeclaredField(cname);
//                    //将字段设置为可访问状态
//                    field.setAccessible(true);
//                    //为字段设置属性值
//                    field.set(obj, value);
//                }
//            }
//        }
//        closeResource(conn, null, rs);
//        return obj;
//    }
//
//
//    public boolean batchInsert(Connection conn, String sql, String values, List<Vector<Object>> sqlValuesList, int batchCount) throws Exception {
//        PreparedStatement ps = null;
//        try {
//        	String insertValuesSql = "";
//
//        	int count = 1;
//        	List<Object> list = new ArrayList<>();
//        	for(int i=0; i<sqlValuesList.size(); i++) {
//        		Vector<Object> sqlValues = sqlValuesList.get(i);
//        		for(int j=0; j<sqlValues.size(); j++) {
//        			list.add(sqlValues.get(j));
//        		}
//        		insertValuesSql = values + "," + insertValuesSql;
//        		if(count - batchCount == 0) {
//        			ps = conn.prepareStatement(sql + " values " + insertValuesSql.substring(0, insertValuesSql.length() - 1));
//                    ps.clearParameters();
//                    for(int j=0; j<list.size(); j++) {
//                    	ps.setObject(j+1, list.get(j));
//                    }
//                    ps.executeUpdate();
//
//                    close(ps, null);
//                    count=1;
//                    insertValuesSql= "";
//                    list.clear();
//        		} else {
//        			count++;
//        		}
//        	}
//
//        	if(count > 1) {
//        		ps = conn.prepareStatement(sql + " values " + insertValuesSql.substring(0, insertValuesSql.length() - 1));
//                ps.clearParameters();
//                for(int j=0; j<list.size(); j++) {
//                	ps.setObject(j+1, list.get(j));
//                }
//                ps.executeUpdate();
//        	}
//            return true;
//        } finally {
//            close(ps, null);
//        }
//    }
//}
