package com.example.demo.error;

public class UserNotFoundException extends Exception {
	public UserNotFoundException(String s) {
		super(s);
	}
}
