package com.example.demo.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.demo.entity.ErrorMessage;

@ControllerAdvice
@ResponseStatus
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(InvalidException.class)
	public ResponseEntity<ErrorMessage> func(InvalidException exception, WebRequest request){
		ErrorMessage em = new ErrorMessage(HttpStatus.FORBIDDEN, exception.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(em);
	}
	@ExceptionHandler(LowBalanceException.class)
	public ResponseEntity<ErrorMessage> func1(LowBalanceException exception, WebRequest request){
		ErrorMessage em = new ErrorMessage(HttpStatus.NOT_ACCEPTABLE, exception.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(em);
	}
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorMessage> func2(UserNotFoundException exception, WebRequest request){
		ErrorMessage em = new ErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(em);
	}
	@ExceptionHandler(NoTransactionsException.class)
	public ResponseEntity<ErrorMessage> func3(NoTransactionsException exception, WebRequest request){
		ErrorMessage em = new ErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(em);
	}
	
}
