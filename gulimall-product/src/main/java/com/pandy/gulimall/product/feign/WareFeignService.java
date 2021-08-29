package com.pandy.gulimall.product.feign;

import com.pandy.common.to.es.SkuHasStockVo;
import com.pandy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author Pandy
 * @Date 2021/8/29 11:30
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("ware/waresku/hasStock")
    public R<List<SkuHasStockVo>> getSkusStock(@RequestBody List<Long> skuIds);
}
