package com.jwlz.sjjc.zstp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zstp/dataSource")
public class DataSourceController {

    @RequestMapping(value = "/showDB", method = {RequestMethod.GET, RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public @ResponseBody Object showDB(HttpServletRequest request) {
        return null;
    }

}
