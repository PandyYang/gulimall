package com.pandy.gulimall.search.service;

import com.pandy.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @Author Pandy
 * @Date 2021/8/29 12:23
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
