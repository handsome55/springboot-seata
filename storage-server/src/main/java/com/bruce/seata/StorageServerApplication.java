package com.bruce.seata;

import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 库存服务
 *
 * @author brucelee
 */
@MapperScan("com.bruce.seata.dao")
@EnableAutoDataSourceProxy
@SpringBootApplication
public class StorageServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StorageServerApplication.class, args);
    }

}
