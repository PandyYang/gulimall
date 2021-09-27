package com.pandy.gulimall.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author Pandy
 * @Date 2021/9/27 19:09
 */
@Controller
public class OrderTestController {

    @GetMapping("{path}.html")
    public String test(@PathVariable(value = "path") String path) {
        return path;
    }
}
