package com.example.demo.error;

public class NoTransactionsException extends Exception{
	public NoTransactionsException(String s) {
		super(s);
	}
}
