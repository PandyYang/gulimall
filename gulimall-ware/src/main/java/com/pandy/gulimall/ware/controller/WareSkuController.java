package com.pandy.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.pandy.common.exception.BizCodeEnum;
import com.pandy.common.to.es.SkuHasStockVo;
import com.pandy.gulimall.ware.exception.NoStockException;
import com.pandy.gulimall.ware.vo.LockStockResult;
import com.pandy.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.pandy.gulimall.ware.entity.WareSkuEntity;
import com.pandy.gulimall.ware.service.WareSkuService;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.R;



/**
 * 商品库存
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:45:45
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @PostMapping("/lock")
    public R orderLockStock(WareSkuLockVo vo) {
        try {
            Boolean stockResults = wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch (NoStockException e) {
            return R.error(BizCodeEnum.NOT_STOCK_EXCEPTION.getCode(), BizCodeEnum.NOT_STOCK_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/hasStock")
    public R getSkusStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockVo> skusStock = wareSkuService.getSkusStock(skuIds);
        return R.ok().setData(skusStock);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }



}
