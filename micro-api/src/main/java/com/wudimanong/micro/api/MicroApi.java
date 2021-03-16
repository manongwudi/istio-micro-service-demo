package com.wudimanong.micro.api;

import istio.fake.annotation.EnableFakeClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author jiangqiao
 */
@SpringBootApplication
@EnableFakeClients(basePackages = "com.wudimanong.micro.order.client")
public class MicroApi {

    public static void main(String[] args) {
        SpringApplication.run(MicroApi.class, args);
    }
}
