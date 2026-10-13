package com.example.demo.error;

public class LowBalanceException extends Exception {
	public LowBalanceException(String s) {
		super(s);
	}
}
