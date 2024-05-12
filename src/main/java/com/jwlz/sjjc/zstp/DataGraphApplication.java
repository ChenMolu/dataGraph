package com.jwlz.sjjc.zstp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@SpringBootApplication
@MapperScan("com.jwlz.sjjc.zstp.mapper")
public class DataGraphApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataGraphApplication.class, args);
    }

}
