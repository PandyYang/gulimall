package com.pandy.gulimall.member.exception;

public class PhoneExistException extends RuntimeException {
	public PhoneExistException() {
		super("手机号存在");
	}
}
