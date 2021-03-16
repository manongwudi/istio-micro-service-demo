package com.wudimanong.micro.order.service;


import com.wudimanong.micro.order.client.bo.CreateOrderBO;
import com.wudimanong.micro.order.client.dto.CreateOrderDTO;

/**
 * @author jiangqiao
 */
public interface OrderService {

    /**
     * 下单接口
     *
     * @param createOrderDTO
     * @return
     */
    CreateOrderBO create(CreateOrderDTO createOrderDTO);
}
