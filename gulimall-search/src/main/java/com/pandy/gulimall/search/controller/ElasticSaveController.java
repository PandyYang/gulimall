package com.pandy.gulimall.search.controller;

import com.pandy.common.exception.BizCodeEnum;
import com.pandy.common.to.es.SkuEsModel;
import com.pandy.common.utils.R;
import com.pandy.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @Author Pandy
 * @Date 2021/8/29 12:21
 */

@Slf4j
@RequestMapping("/search")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) throws IOException {

        boolean b = false;

        try {
            b = !productSaveService.productStatusUp(skuEsModels);
        }catch (Exception e) {
            b = false;
            log.error("上架出现异常", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }

        if (b)
            return R.ok();
        return R.error();
    }

}
