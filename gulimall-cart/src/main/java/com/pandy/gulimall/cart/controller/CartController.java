package com.pandy.gulimall.cart.controller;

import com.pandy.common.constant.AuthServerConstant;
import com.pandy.gulimall.cart.interceptor.CartInterceptor;
import com.pandy.gulimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * @Author Pandy
 * @Date 2021/9/23 22:20
 */
@Controller
public class CartController {

    /**
     * userkey 标识用户身份 一个月之后过期
     * 如果第一次使用jd的购物车功能 都会给一个临时的用户身份
     * 浏览器以后保存 每次访问都会带上这个cookie
     *
     * 登录 session有
     * 没登录 按照cookie里面
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage() {

        // 快速获取用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        System.out.println(userInfoTo);
        return "cartList";
    }
}
