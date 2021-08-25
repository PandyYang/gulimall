package com.pandy.gulimall.product.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.pandy.gulimall.product.service.AttrAttrgroupRelationService;
import com.pandy.gulimall.product.vo.AttrGroupRelationVo;
import com.pandy.gulimall.product.vo.AttrRespVo;
import com.pandy.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.pandy.gulimall.product.entity.AttrEntity;
import com.pandy.gulimall.product.service.AttrService;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.R;

import javax.annotation.Resource;


/**
 * 商品属性
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-10 23:46:58
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Resource
    private AttrAttrgroupRelationService relationService;

    @GetMapping("/base/list/{categoryId}")
    public R baseAttrList(@RequestParam Map<String, Object> params, @PathVariable Long categoryId) {
        PageUtils page = attrService.queryBaseAttr(params, categoryId);
        return R.ok().put("page", page);
    }

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos){
        relationService.saveBatch(vos);
        return R.ok();
    }

    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params ,@PathVariable("catelogId") Long catelogId, @PathVariable("attrType") String attrType){

        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, attrType);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) throws InvocationTargetException, IllegalAccessException {
//		AttrEntity attr = attrService.getById(attrId);

		AttrRespVo attrRespVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrRespVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attrVo) throws InvocationTargetException, IllegalAccessException {
		attrService.saveAttr(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr) throws InvocationTargetException, IllegalAccessException {
//		attrService.updateById(attr);

		attrService.updateAttr(attr);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }
}
