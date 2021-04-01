package com.wudimanong.micro.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author jiangqiao
 */
@Component
@Slf4j
public class GrpcClientCommandLineRunner implements CommandLineRunner {

    @Autowired
    GrpcClientConfiguration configuration;

    @Override
    public void run(String... args) throws Exception {
        //开启gRPC客户端
        configuration.start();

        //添加客户端关闭的逻辑
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                configuration.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }
}
