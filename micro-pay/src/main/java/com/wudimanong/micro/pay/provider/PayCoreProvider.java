package com.wudimanong.micro.pay.provider;

import com.wudimanong.micro.pay.proto.PayRequest;
import com.wudimanong.micro.pay.proto.PayResponse;
import com.wudimanong.micro.pay.proto.PayServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author jiangqiao
 */
@Slf4j
@Component
public class PayCoreProvider extends PayServiceGrpc.PayServiceImplBase {

    /**
     * 实现ProtoBuf中定义的服务方法
     *
     * @param request
     * @param responseStreamObserver
     */
    @Override
    public void doPay(PayRequest request, StreamObserver<PayResponse> responseStreamObserver) {
        //逻辑处理(简单模拟打印日志)
        log.info("处理gRPC支付处理请求,orderId->{};payAmount{}", request.getOrderId(), request.getAmount());
        //构建返回对象(构建处理状态)
        PayResponse response = PayResponse.newBuilder().setStatus(2).build();
        //设置数据响应
        responseStreamObserver.onNext(response);
        responseStreamObserver.onCompleted();
    }

}
