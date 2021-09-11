package com.pandy.gulimall.search.vo;

import com.pandy.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @Author Pandy
 * @Date 2021/9/6 22:17
 */
@Data
public class SearchResult {

    // 查询到的所有商品信息
    private List<SkuEsModel> products;

    private Integer pageNum;

    private Long total;

    private Integer totalPages;

    private List<BrandVo> brands;

    private List<CatalogVo> catalogs;

    private List<AttrVo> attrs;

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attValue;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }
}
