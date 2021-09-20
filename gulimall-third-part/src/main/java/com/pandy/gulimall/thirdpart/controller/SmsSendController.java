package com.pandy.gulimall.thirdpart.controller;

import com.pandy.common.utils.R;
import com.pandy.gulimall.thirdpart.component.Smscomponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Pandy
 * @Date 2021/9/20 13:40
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    Smscomponent smscomponent;

    @GetMapping("")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smscomponent.sendSmsCode(phone, code);
        return R.ok();
    }
}
