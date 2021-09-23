package com.pandy.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @Author Pandy
 * @Date 2021/9/23 22:38
 */
@Data
@ToString
public class UserInfoTo {

    private Long userId;
    private String userKey;

    private boolean tempUser = false;
}
