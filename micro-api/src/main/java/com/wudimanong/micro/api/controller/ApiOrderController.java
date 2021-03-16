package com.wudimanong.micro.api.controller;

import com.wudimanong.micro.api.exception.ServiceException;
import com.wudimanong.micro.order.client.OrderServiceClient;
import com.wudimanong.micro.order.client.bo.CreateOrderBO;
import com.wudimanong.micro.order.client.dto.CreateOrderDTO;
import com.wudimanong.micro.order.client.dto.result.GlobalCodeEnum;
import com.wudimanong.micro.order.client.dto.result.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jiangqiao
 */
@RestController
@RequestMapping("/api/order")
public class ApiOrderController {

    /**
     * 订单微服务api接口依赖
     */
    @Autowired
    OrderServiceClient orderServiceClient;

    /**
     * 下单接口
     *
     * @param createOrderDTO
     * @return
     */
    @PostMapping("/create")
    public CreateOrderBO create(@RequestBody CreateOrderDTO createOrderDTO) {
        ResponseResult<CreateOrderBO> result = orderServiceClient.create(createOrderDTO);
        if (result.getCode().equals(GlobalCodeEnum.GL_SUCC_0.getCode())) {
            return result.getData();
        } else {
            throw new ServiceException(result.getCode(), result.getMessage());
        }
    }
}
