package com.pandy.gulimall.product.web;

import com.pandy.gulimall.product.service.SkuInfoService;
import com.pandy.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.AccessType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author Pandy
 * @Date 2021/9/13 21:41
 */

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;


    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable Long skuId, Model model) {

        SkuItemVo vo = skuInfoService.item(skuId);
        System.out.println("准备查询" + skuId + "详情");
        model.addAttribute("item", vo);
        return "item.html";
    }

}
