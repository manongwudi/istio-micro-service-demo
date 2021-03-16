对于传统采用Spring Cloud框架构建的微服务，服务之间一般会通过FeignClient方式进行微服务调用。但在Service Mesh微服务架构下，微服务之间的通信调用则不再需要原生OpenFeign所提供的客户端负载、熔断等功能。

但是为了快速支持或迁移Spring Cloud微服务至Service Mesh体系，需要在服务调用编程方式上尽量保持原有FeignClient的调用方式，但需要去掉其中所有熔断、负载均衡相关的服务治理代码。在Service Mesh体系中这些功能将交给Istio去完成。

本项目的编写目的就是为了适应上述要求，具体功能如下：

1、支持在istio服务网格体系下，完成服务间的快速调用（体验和原先Spring Cloud Feign类似）；

2、支持多环境配置，例如本地环境微服务的调用地址可配置为本地，其他环境默认为Kubernetes集群中的服务；

3、支持链路追踪，默认透传如下Header，可以自动支持jaeger、zipkin链路追踪；
`"x-request-id", "x-b3-traceid", "x-b3-spanid", "x-b3-sampled", "x-b3-flags", "x-b3-parentspanid","x-ot-span-context", "x-datadog-trace-id", "x-datadog-parent-id", "x-datadog-sampled", "end-user", "user-agent"
`
