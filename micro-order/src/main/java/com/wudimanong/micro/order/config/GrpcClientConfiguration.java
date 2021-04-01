package com.wudimanong.micro.order.config;

import com.wudimanong.micro.pay.proto.PayServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author jiangqiao
 */
@Slf4j
@Component
public class GrpcClientConfiguration {

    /**
     * 支付gRPC Server的地址
     */
    @Value("${server-host}")
    private String host;

    /**
     * 支付gRPC Server的端口
     */
    @Value("${server-port}")
    private int port;

    private ManagedChannel channel;

    /**
     * 支付服务stub对象
     */
    private PayServiceGrpc.PayServiceBlockingStub stub;

    public void start() {
        //开启channel
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        //通过channel获取到服务端的stub
        stub = PayServiceGrpc.newBlockingStub(channel);
        log.info("gRPC client started, server address: {}:{}", host, port);
    }

    public void shutdown() throws InterruptedException {
        //调用shutdown方法后等待1秒关闭channel
        channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
        log.info("gRPC client shut down successfully.");
    }

    public PayServiceGrpc.PayServiceBlockingStub getStub() {
        return this.stub;
    }
}
