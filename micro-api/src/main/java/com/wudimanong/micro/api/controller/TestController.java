package com.wudimanong.micro.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jiangqiao
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "无依赖测试接口返回->OK!";
    }
}
