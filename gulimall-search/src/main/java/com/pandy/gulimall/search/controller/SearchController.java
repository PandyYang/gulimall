package com.pandy.gulimall.search.controller;

import com.pandy.gulimall.search.service.MallSearchService;
import com.pandy.gulimall.search.vo.SearchParam;
import com.pandy.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.xml.ws.Action;
import java.io.IOException;

/**
 * @Author Pandy
 * @Date 2021/9/6 21:27
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model) throws IOException {
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
