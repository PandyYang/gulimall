package com.pandy.gulimall.member.dao;

import com.pandy.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:32:27
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
