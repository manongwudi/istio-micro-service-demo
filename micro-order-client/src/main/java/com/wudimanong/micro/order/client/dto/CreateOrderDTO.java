package com.wudimanong.micro.order.client.dto;

import lombok.Data;

/**
 * @author jiangqiao
 */
@Data
public class CreateOrderDTO {

    /**
     * 业务订单号
     */
    private String businessId;

    /**
     * 下单金额
     */
    private Integer amount;

    /**
     * 下单渠道
     */
    private Integer channel;

}
