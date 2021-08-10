package com.pandy.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pandy.gulimall.product.entity.BrandEntity;
import com.pandy.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {

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

}
