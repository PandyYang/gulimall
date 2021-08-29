package com.pandy.gulimall.product.feign;

import com.pandy.common.to.es.SkuEsModel;
import com.pandy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author Pandy
 * @Date 2021/8/29 12:47
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {

    @PostMapping("/search/product/")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
