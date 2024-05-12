package com.jwlz.sjjc.zstp.utils;

import com.alibaba.fastjson2.JSONObject;
import com.jwlz.sjjc.zstp.exception.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    public static String sendGet(String url, String param, int timeout) {
        StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        JSONObject json = JSONObject.parseObject("");
        try {
            json = new JSONObject();
            System.out.println("parameter: " + param);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            param = URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            json.put("StatusCode", 500);
            json.put("StatusMsg", "Broken VM does not support UTF-8");
            return json.toString();
        }
        long t1 = System.currentTimeMillis();
        try {
            String urlNameString = url + "/" + param;
            System.out.println("request: " + urlNameString);
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            //set agent to avoid: speed limited by server if server think the client not a browser
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            // 建立实际的连接
            connection.connect();

            long t0 = System.currentTimeMillis(); //ms

            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();

            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line + "\n");
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            json.put("StatusCode", 999);
            json.put("StatusMsg", "HTTP GET Error:" + e.getMessage());
            return json.toString();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage());
            }
            long t2 = System.currentTimeMillis(); //ms
            System.out.println("url:" + url + "\n参数：" + param + "\n结果：" + (result.length() > 50000 ? (result.substring(0, 50000) + "......") : result) + "\n耗时(ms)：" + (t2 - t1));
            if(StringUtils.isEmpty(result.toString())) {
                throw new BadRequestException("访问超时或数据库服务异常");
            }
        }
        if(url.endsWith("grpc/api") && StringUtils.isNotEmpty(result)) {
            return result.toString().replace("@type", "grpc_type");
        }
        return result.toString();
    }


    public static String sendPost(String url, String strUrl, String strPost, int timeout) {
        PrintWriter out = null;
        StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        if (strUrl.isEmpty() == false) {
            try {
                strUrl = URLEncoder.encode(strUrl, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("Broken VM does not support UTF-8");
            }
        }
        strUrl = url + "/" + strUrl;
        long t1 = System.currentTimeMillis(); // ms
        try {
            URL realUrl = new URL(strUrl);

            // open the connection with the URL
            URLConnection connection = realUrl.openConnection();

            // set request properties
            if(url.endsWith("grpc/api")) {
                connection.setRequestProperty("Content-Type", "application/json;");
            } else {
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;");
            }
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            out = new PrintWriter(connection.getOutputStream());
            out.print(strPost);
            out.flush();

            // get all response header fields
            Map<String, List<String>> map = connection.getHeaderFields();

            // define BufferedReader to read the response of the URL
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line + "\n");
            }
        } catch (Exception e) {
            logger.error("error in post request: " + e);
        }

        // use finally to close the input stream
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
            long t2 = System.currentTimeMillis(); //ms
            System.out.println("url:" + strUrl + "\n参数：" + strPost + "\n结果：" + (result.length() > 50000 ? (result.substring(0, 50000) + "......") : result) + "\n耗时(ms)：" + (t2 - t1));
            if(StringUtils.isEmpty(result.toString())) {
                throw new BadRequestException("访问超时或数据库服务异常");
            }
        }
        return result.toString();
    }

    public static String sendPost(String url, String strPost, int timeout) {
        PrintWriter out = null;
        StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        long t1 = System.currentTimeMillis(); // ms
        try {
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            if(url.endsWith("grpc/api")) {
                connection.setRequestProperty("Content-Type", "application/json;");
            } else {
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;");
            }
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            out = new PrintWriter(connection.getOutputStream());
            out.print(strPost);
            out.flush();

            // define BufferedReader to read the response of the URL
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line + "\n");
            }
        } catch (Exception e) {
            logger.error("error in post request: " + e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
            long t2 = System.currentTimeMillis(); //ms
            System.out.println("url:" + url + "\n参数：" + strPost + "\n结果：" + (result.length() > 50000 ? (result.substring(0, 50000) + "......") : result) + "\n耗时(ms)：" + (t2 - t1));
            if(StringUtils.isEmpty(result.toString())) {
                throw new BadRequestException("访问超时或数据库服务异常");
            }
        }
        return result.toString();
    }
}
