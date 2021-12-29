package com.pandy.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {

	private MemberAddressVo memberAddressVo;

	private BigDecimal fare;

	public MemberAddressVo getMemberAddressVo() {
		return memberAddressVo;
	}

	public void setMemberAddressVo(MemberAddressVo memberAddressVo) {
		this.memberAddressVo = memberAddressVo;
	}

	public BigDecimal getFare() {
		return fare;
	}

	public void setFare(BigDecimal fare) {
		this.fare = fare;
	}

}
