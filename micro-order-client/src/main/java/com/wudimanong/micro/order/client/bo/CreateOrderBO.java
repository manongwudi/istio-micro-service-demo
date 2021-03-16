package com.wudimanong.micro.order.client.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jiangqiao
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderBO {

    /**
     * 订单号
     */
    private String orderId;
    /**
     * 订单ID
     */
    private Integer status;
}
