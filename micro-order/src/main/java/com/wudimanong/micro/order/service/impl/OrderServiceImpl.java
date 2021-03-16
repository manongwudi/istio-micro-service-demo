package com.wudimanong.micro.order.service.impl;

import com.wudimanong.micro.order.client.bo.CreateOrderBO;
import com.wudimanong.micro.order.client.dto.CreateOrderDTO;
import com.wudimanong.micro.order.service.OrderService;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author jiangqiao
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Override
    public CreateOrderBO create(CreateOrderDTO createOrderDTO) {
        log.info("现在开始处理下单请求.....");
        //生成订单号
        String orderId = String.valueOf(new Random(100).nextInt(100000) + System.currentTimeMillis());
        return CreateOrderBO.builder().orderId(orderId).status(0).build();
    }
}
