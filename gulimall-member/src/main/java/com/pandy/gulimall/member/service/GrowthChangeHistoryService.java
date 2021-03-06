package com.pandy.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.member.entity.GrowthChangeHistoryEntity;

import java.util.Map;

/**
 * 成长值变化历史记录
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:32:26
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

