package com.wudimanong.micro.order.client;

import com.wudimanong.micro.order.client.bo.CreateOrderBO;
import com.wudimanong.micro.order.client.dto.CreateOrderDTO;
import com.wudimanong.micro.order.client.dto.result.ResponseResult;
import istio.fake.annotation.FakeClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author jiangqiao
 */
@FakeClient(name = "micro-order")
@RequestMapping("/order")
public interface OrderServiceClient {

    /**
     * 订单创建
     *
     * @param createOrderDTO
     * @return
     */
    @PostMapping("/create")
    ResponseResult<CreateOrderBO> create(@RequestBody CreateOrderDTO createOrderDTO);

}
