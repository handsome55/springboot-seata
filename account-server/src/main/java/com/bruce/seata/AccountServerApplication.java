package com.bruce.seata;

import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 账户服务
 *
 * @author brucelee
 */

@MapperScan("com.bruce.seata.dao")
@EnableFeignClients
@EnableAutoDataSourceProxy
@SpringBootApplication
public class AccountServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountServerApplication.class, args);
    }
}
