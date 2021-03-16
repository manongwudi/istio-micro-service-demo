package com.wudimanong.micro.api.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * @author jiangqiao
 */
@Configuration
public class WebMvcConfig implements InitializingBean {

    @Autowired
    RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Override
    public void afterPropertiesSet() {
        List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerAdapter
                .getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> list = new ArrayList<>();
        //自定义returnHandler
        list.add(new AutoResultReturnHandler());
        list.addAll(returnValueHandlers);
        requestMappingHandlerAdapter.setReturnValueHandlers(list);
    }
}
