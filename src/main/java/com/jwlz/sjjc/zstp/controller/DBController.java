package com.jwlz.sjjc.zstp.controller;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jwlz.sjjc.zstp.common.R;
import com.jwlz.sjjc.zstp.utils.GStoreConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@Slf4j
@RestController
@RequestMapping("/zstp/db")
public class DBController {

    @GetMapping(value = "/showdb")
    public R<JSONArray> showdb() {
        String ip = "61.136.101.220", port = "20046", version = "1.3", gstore_username = "root", gstore_password = "123456", accessType = "ghttp";
        GStoreConnector gc = GStoreConnector.getGStoreConnector(version, ip, Integer.valueOf(port), gstore_username, gstore_password, accessType);
        String result = gc.show();
        JSONObject gStoreJson = JSONObject.parseObject(result);
        if (gStoreJson.getInteger("StatusCode") == 0) {
            String responseBody = gStoreJson.getString("ResponseBody");
            JSONArray array = JSONArray.parseArray(responseBody);
            return R.success(array);
        } else {
            return R.error("查询异常");
        }
    }

}
