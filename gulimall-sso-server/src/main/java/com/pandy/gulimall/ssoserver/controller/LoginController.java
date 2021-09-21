package com.pandy.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @Author Pandy
 * @Date 2021/9/21 19:27
 */
@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @PostMapping("/doLogin")
    public String doLogin(String username, String password, String url, HttpServletResponse response){
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            Cookie sso_cookie = new Cookie("sso_token", uuid);
            response.addCookie(sso_cookie);
            stringRedisTemplate.opsForValue().set(uuid, username);
            return "redirect:" + url + "?token=" + uuid;
        }
        return "login";
    }

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url, Model model,
                            @CookieValue(value = "sso_token", required = false) String sso_token) {

        if (!StringUtils.isEmpty(sso_token)) {
            return "redirect:" + url + "?token=" + sso_token;
        }

        model.addAttribute("url", url);
        return "login";
    }
}
