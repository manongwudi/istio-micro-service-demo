package com.wudimanong.micro.order.service.impl;

import com.wudimanong.micro.order.client.bo.CreateOrderBO;
import com.wudimanong.micro.order.client.dto.CreateOrderDTO;
import com.wudimanong.micro.order.config.GrpcClientConfiguration;
import com.wudimanong.micro.order.service.OrderService;
import com.wudimanong.micro.pay.proto.PayRequest;
import com.wudimanong.micro.pay.proto.PayResponse;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jiangqiao
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    /**
     * 引入gRPC客户端配置依赖
     */
    @Autowired
    GrpcClientConfiguration gRpcClent;

    @Override
    public CreateOrderBO create(CreateOrderDTO createOrderDTO) {
        log.info("现在开始处理下单请求.....");
        //生成订单号
        String orderId = String.valueOf(new Random(100).nextInt(100000) + System.currentTimeMillis());
        //构建支付请求(gRPC调用)
        PayRequest payRequest = PayRequest.newBuilder().setOrderId(orderId).setAmount(createOrderDTO.getAmount())
                .build();
        //使用stub发送请求到服务端
        PayResponse payResponse = gRpcClent.getStub().doPay(payRequest);
        log.info("pay gRpc response->" + payResponse.toString());
        return CreateOrderBO.builder().orderId(orderId).status(payResponse.getStatus()).build();
    }
}
