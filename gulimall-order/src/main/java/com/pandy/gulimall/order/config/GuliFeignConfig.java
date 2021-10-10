package com.pandy.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author Pandy
 * @Date 2021/10/10 10:54
 */
@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {

                // 使用contextHolder拿到进去的请求数据
                ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = attributes.getRequest(); // 老请求
                // 同步请求数据 Cookie
                if (request != null) {
                    // 给新请求同步老请求得header
                    String cookie = request.getHeader("Cookie");
                    requestTemplate.header("Cookie", cookie);
                    System.out.println("在远程调用之前先进行拦截.");
                }
            }
        };
    }
}
