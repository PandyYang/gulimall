package com.pandy.gulimall.thirdpart;


import com.aliyun.oss.OSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartApplicationTests {

    @Autowired
    OSSClient ossClient;

    @Test
    public void testUploadFile() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("C:\\Users\\Pandy\\Desktop\\123.png");

        ossClient.putObject("pandy-gulimall", "312.png", inputStream);

        ossClient.shutdown();

        System.out.println("上传完成！");
    }

}
