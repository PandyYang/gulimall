package com.pandy.gulimall.thirdpart.component;

import com.aliyuncs.http.HttpResponse;
import com.pandy.common.utils.HttpUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Pandy
 * @Date 2021/9/20 12:41
 */
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Data
@Component
public class Smscomponent {

    private String host;
    private String path;
    private String sign;
    private String templateId;
    private String appCode;

    public void sendSmsCode(String phone, String code) {
        String method = "POST";
        String appcode = appCode;
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", code);
        querys.put("smsSignId", sign);
        querys.put("templateId",templateId);
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = (HttpResponse) HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
