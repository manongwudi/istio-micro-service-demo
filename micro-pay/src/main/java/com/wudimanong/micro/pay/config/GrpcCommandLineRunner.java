package com.wudimanong.micro.pay.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author jiangqiao
 */
@Component
public class GrpcCommandLineRunner implements CommandLineRunner {

    @Autowired
    GrpcServerConfiguration configuration;

    @Override
    public void run(String... args) throws Exception {
        configuration.start();
        configuration.block();

    }
}
