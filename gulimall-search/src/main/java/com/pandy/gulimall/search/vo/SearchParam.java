package com.pandy.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Pandy
 * @Date 2021/9/6 21:55
 * 封装页面所有可能传递过来的查询条件
 */
@Data
public class SearchParam {

    // 页面传递的参数 全文匹配关键字
    private String keyword;

    // 三级分类Id
    private Long catalog3Id;

    // 排序条件
    private String sort;

    // 综合排序
    private Integer hasStock;

    // 价格区间
    private String skuPrice;

    private List<Long> brandId;

    private List<String> attrs;

    private Integer pageNum;
}
