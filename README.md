# istio-micro-service-demo
基于Spring Boot+Istio的Service Mesh微服务架构示例代码

模拟App客户端服务调用的服务架构，其调用链路如下：

           micro-api(面向外部客户端的Api服务)
                  |
                  | http协议
                  |
             micro-order(内部订单服务)
                  |
                  | Grpc协议
                  |
              mciro-pay(内部支付服务)
              
如上所示链路，具体说明如下：

1)、为了完整演示在Service Mesh架构下的微服务研发过程，这里我们定义三个微服务，其中micro-api服务是面向外部客户端的接入Api服务提供Http协议访问；

2）、而micro-api与micro-order则基于微服务的注册发现机制进行内部微服务调用访问，采用Http协议；

3）、micro-order与micro-pay之间也基于微服务注册发现机制进行内部微服务调用访问，为了演示多种场景，这里两个微服务的调用采用GRpc协议;  


更详细的文章说明链接：
https://mp.weixin.qq.com/s/L1LoiI9NZqwZWsuCzEJN1A            