package com.pandy.gulimall.auth.vo;

import lombok.Data;

/**
 * @Author Pandy
 * @Date 2021/9/19 17:34
 */
@Data
public class SocialUser {
    private String accessToken;

    private String remindIn;

    private int expiresIn;

    private String uid;

    private String isrealname;
}
