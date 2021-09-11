package com.pandy.gulimall.search.service;

import com.pandy.gulimall.search.vo.SearchParam;
import com.pandy.gulimall.search.vo.SearchResult;

import java.io.IOException;

/**
 * @Author Pandy
 * @Date 2021/9/6 21:55
 */
public interface MallSearchService {

    public SearchResult search(SearchParam searchParam) throws IOException;
}
