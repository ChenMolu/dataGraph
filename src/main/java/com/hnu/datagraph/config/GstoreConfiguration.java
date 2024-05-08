package com.hnu.datagraph.config;

import com.hnu.datagraph.jgsc.GstoreConnector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GstoreConfiguration {

    private static final String IP = "10.112.41.37";
    private static final Integer PORT = 9999;
    private static final String HTTP_TYPE = "ghttp";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";


    @Bean
    public GstoreConnector gstoreConfig() {
        GstoreConnector gstoreConnector = new GstoreConnector(IP, PORT, HTTP_TYPE, USER, PASSWORD);
        return gstoreConnector;
    }
}
