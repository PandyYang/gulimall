package com.pandy.gulimall.product;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pandy.gulimall.product.entity.BrandEntity;
import com.pandy.gulimall.product.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void contextLoads() {

        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setDescript("123");
//        brandEntity.setName("321");
//        brandService.save(brandEntity);
//        System.out.println("save success");


//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("1111111");
//        brandService.updateById(brandEntity);


        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach(System.out::println);
    }

    @Test
    public void test() {
        System.out.println(123);
    }

    @Test
    public void RedisTest() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello", "world" + UUID.randomUUID().toString());

        String hello = ops.get("hello");
        System.out.println(hello);
    }

}
