package com.wudimanong.micro.order.controller;

import com.wudimanong.micro.order.client.bo.CreateOrderBO;
import com.wudimanong.micro.order.client.dto.CreateOrderDTO;
import com.wudimanong.micro.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jiangqiao
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderService orderServiceImpl;

    /**
     * 下单接口
     *
     * @param createOrderDTO
     * @return
     */
    @PostMapping("/create")
    public CreateOrderBO create(@RequestBody CreateOrderDTO createOrderDTO) {
        return orderServiceImpl.create(createOrderDTO);
    }
}
