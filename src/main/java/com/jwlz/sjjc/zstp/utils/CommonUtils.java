package com.jwlz.sjjc.zstp.utils;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jwlz.sjjc.zstp.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void createRelationMappingDir(HttpServletRequest request, String projectid) {
        String path = request.getServletContext().getRealPath("/") + "/relation-mapping";
        // 先创建一下relation-mapping文件夹
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String dirPath = path + File.separator + projectid;
        File fileDir = new File(dirPath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
    }

    public static String readJsonFile(String fileName, String charset) {
        if (StringUtils.isEmpty(charset)) {
            charset = "utf-8";
        }
        String jsonStr = "";
        FileReader fileReader = null;
        Reader reader = null;
        try {
            File jsonFile = new File(fileName);
            if(!jsonFile.exists()) {
                return null;
            }
            fileReader = new FileReader(jsonFile);
            reader = new InputStreamReader(new FileInputStream(jsonFile), charset);
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            try {
                if(null != fileReader) {
                    fileReader.close();
                }
                if(null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readRemoteJsonFile(String fileName, String charset) {
        try {
            URL url = new URL(fileName);
            InputStream is = url.openStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            StringBuffer sb = new StringBuffer("");
            int len = 0;
            byte[] temp = new byte[1024];
            while ((len = bis.read(temp)) != -1) {
                sb.append(new String(temp, 0, len, charset));
            }
            is.close();
            bis.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }
        // 目录此时为空，可以删除
        dir.delete();
    }

    public static boolean isJson(String content) {
        if(StringUtils.isEmpty(content)){
            return false;
        }
        boolean isJsonObject = true;
        boolean isJsonArray = true;
        try {
            JSONObject.parseObject(content);
        } catch (Exception e) {
            isJsonObject = false;
        }
        try {
            JSON.parseArray(content);
        } catch (Exception e) {
            isJsonArray = false;
        }
        if(!isJsonObject && !isJsonArray){ //不是json格式
            return false;
        }
        return true;
    }

//    public static void updateBindPassword(String url, String username, String password) {
//        long timemills = System.currentTimeMillis();
//        String mac = getMac(timemills, "updateBindPassword");
//        String param = "pcode=g003&username=" + username + "&password=" + password + "&timemills=" + timemills + "&mac=" + mac;
//        HttpHelper.sendGet2(url, param, 30 * 1000);
//    }
//
//    public static void checkNotice(String url, String username) {
//        long timemills = System.currentTimeMillis();
//        String mac = getMac(timemills, "updateCheckStatus");
//        String param = "pcode=g003&username=" + username + "&timemills=" + timemills + "&mac=" + mac;
//        HttpHelper.sendGet2(url, param, 30 * 1000);
//    }

//    public static String getMac(long timemills, String uri) {
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(timemills);
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String date = format.format(cal.getTime());
//        String mac = MD5Util.MD5Encode(date + uri, "UTF-8");
//        return mac;
//    }

//    public static boolean checkMac(String timemills, String mac, String uri) {
//        if (StringUtils.isEmpty(timemills) || StringUtils.isEmpty(mac)) {
//            return false;
//        }
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(Long.valueOf(timemills));
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String date = format.format(cal.getTime());
//        String mac_back = MD5Util.MD5Encode(date + uri.substring(uri.lastIndexOf("/") + 1), "UTF-8");
//        logger.info("mac_back:"+mac_back);
//        if (mac_back.equals(mac)) {
//            return true;
//        }
//        return false;
//    }

    public static Map[] mapToList(Map map) {
        Iterator it = map.keySet().iterator();
        Map[] array = new HashMap[map.keySet().size()];
        int i = 0;
        while(it.hasNext()) {
            Map sub = new HashMap();
            Object key = it.next();
            sub.put(key, map.get(key));
            array[i] = sub;
            i++;
        }
        return array;
    }
    public static String unescape(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src
                            .substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src
                            .substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    public static String escape(String src) {
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j)
                    || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    public static String getDbName(String dbname, String flag, String userid) {
        if("1".equals(flag)) {
            return dbname + "_" + userid;
        } else {
            return dbname;
        }
    }

    public static <T> boolean isContains(T[] list, T target) {
        for(T sub : list) {
            if(sub.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public static String getCurrentDateStr() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(cal.getTime());
        return date;
    }

    public static long getFileLineNumber(String filepath) {
        try {
            File file = new File(filepath);
            if (file.exists()) {
                long fileLength = file.length();
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                lineNumberReader.skip(fileLength);
                long lines = lineNumberReader.getLineNumber();

                lineNumberReader.close();
                return lines;
            } else {
                System.out.println("File does not exists!");
                return 0;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 替换实体中的非法字符
     * @param input
     * @return
     */
    public static String replaceIRI(String input) {
    	// 空格变成下划线
    	Pattern pattern1 = Pattern.compile("[\\s]");
    	Matcher matcher1= pattern1.matcher(input);
    	input = matcher1.replaceAll("_");
        Pattern pattern = Pattern.compile("[\\a\\f\\n\\r\\t\\v\\\b<>\"{}|^\\\\`]");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }

    public static String replaceLiteral(String input) {
        Pattern pattern = Pattern.compile("[\\n\\r\\\\\"]");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }
    
    private static final String LINE_END = "\r\n";
    private static final String TWO_HYPHENS = "--";
    public static String sendFile(String requestURL, File file, String username, String password) {
    	String BOUNDARY = "----" + getUUID(); // 自定义边界字符串，应全局唯一
    	BufferedReader in = null;
        DataOutputStream dos = null;
        InputStream is = null;
        HttpURLConnection connection = null;
        try  {
        	URL url = new URL(requestURL);
        	connection = (HttpURLConnection) url.openConnection();
        	connection.setDoOutput(true);
        	connection.setDoInput(true);
            connection.setUseCaches(false);
//            connection.setFixedLengthStreamingMode(getFormDataLength(file, BOUNDARY, username, password));//主要是这句，用来禁掉缓存，不过需要将上传数据的大小传进去
        	connection.setRequestMethod("POST");
        	connection.setConnectTimeout(15000); // 设置连接超时时间为15秒
        	connection.setReadTimeout(300000); // 设置读取超时时间为5分钟
        	connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        	dos = new DataOutputStream(connection.getOutputStream());
        	// 添加表单字段（如果有多个字段则多次调用此段代码）
            dos.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
            // 添加表单字段（如果有多个字段则多次调用此段代码）
            dos.writeBytes("Content-Disposition: form-data; name=\"username\""+ LINE_END);
            dos.writeBytes(LINE_END);
            dos.writeBytes(username); 
            dos.writeBytes(LINE_END);
            
            // 添加表单字段（如果有多个字段则多次调用此段代码）
            dos.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
            dos.writeBytes("Content-Disposition: form-data; name=\"password\""+ LINE_END);
            dos.writeBytes(LINE_END);
            dos.writeBytes(password); 
            dos.writeBytes(LINE_END);

            // 添加文件部分
            dos.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + LINE_END);
            dos.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName()) + LINE_END);
            dos.writeBytes(LINE_END);
            // 写入文件内容
            is = new FileInputStream(file);
            byte[] buffer = new byte[1024*10];
            int read;
            while ((read = is.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
                dos.flush();
            }
            
            // 结束文件部分
            dos.writeBytes(LINE_END);

            // 结束整个表单数据
            dos.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);
            
            // 刷新输出流，确保所有数据都已经写出
            dos.flush();

            // 获取服务器响应码
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code : " + responseCode);
            if (responseCode == 200) {
            	StringBuffer result = new StringBuffer();
                in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line + "\n");
                }
                return result.toString();
            } else {
            	throw new BadRequestException("上传文件异常");
            }
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        	throw new BadRequestException("上传文件失败");
        } finally {
        	try {
	        	if(null != connection) {
	        		connection.disconnect();
	        	}
	        	if(null != in) {
					in.close();
	        	}
	        	if(null != is) {
	        		is.close();
	        	}
	        	if(null != dos) {
	        		dos.close();
	        	}
        	} catch (IOException e) {
        		logger.error(e.getMessage(), e);
			}
        }
    }
    
    // 计算表单数据的整体长度（包括边界字符串和文件内容）
    private static long getFormDataLength(File file, String BOUNDARY, String username, String password) {
        long formDataLength = 0;

        // 添加非文件参数的长度
        formDataLength += getFormFieldLength(BOUNDARY, "username", "root");
        formDataLength += getFormFieldLength(BOUNDARY, "password", "123456");
        // 添加文件参数的长度
        formDataLength += getFileFieldLength(BOUNDARY, file);
        // 计算边界字符串的长度
        formDataLength += BOUNDARY.length() + TWO_HYPHENS.length()*2 + LINE_END.length() * 2; // 每个部分的开始和结束各一次换行符，以及结尾的结束边界
        return formDataLength;
    }
    
    private static long getFormFieldLength(String BOUNDARY, String fieldName, String fieldValue) {
        return ("--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n" + fieldValue + "\r\n").getBytes(StandardCharsets.UTF_8).length;
    }
     // 计算文件参数的长度
    private static long getFileFieldLength(String BOUNDARY, File file) {
        long fileSize = file.length();
        long headerLength = ("--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\nContent-Type: " + URLConnection.guessContentTypeFromName(file.getName()) + "\r\n\r\n").getBytes(StandardCharsets.UTF_8).length;
        return headerLength + fileSize + 2; // 加上最后的换行符
    }
    
    public static void main(String[] args) {
    	try {
    		File file = new File("E:\\desktop\\nt样例\\friend.nt");
			sendFile("http://61.136.101.220:20047/file/upload",file,"root","123456");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
