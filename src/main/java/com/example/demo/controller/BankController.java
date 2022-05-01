package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.TransactionType;
import com.example.demo.entity.Transactions;
import com.example.demo.entity.Users;
import com.example.demo.error.NoTransactionsException;
import com.example.demo.error.UserNotFoundException;
import com.example.demo.service.BankService;

@RestController
public class BankController {
	
	@Autowired
	BankService service;
	
	@PostMapping("/admin/save/")
	public Users saveUser(@RequestBody Users user) {
		return service.saveUser(user);
	}
	
	@DeleteMapping("/admin/delete/{acno}")
	@Transactional
	public String removeUser(@PathVariable long acno) throws UserNotFoundException {
		return service.removeUser(acno);
	}
	
	@PutMapping("/admin/update/{acno}")
	public Users updateUser(@PathVariable long acno,@RequestBody Users user) throws UserNotFoundException {
		return service.updateUser(acno, user);
	}
	
	@GetMapping("/admin/")
	public List<Users> getUsers(){
		return service.getUsers();
	}
	
	@GetMapping("/admin/{acno}")
	public Users getUser(@PathVariable long acno) throws UserNotFoundException {
		return service.getUser(acno);
	}
	
	@GetMapping("/admin/getTrans/{n}")
	public List<Transactions> getTrans(@PathVariable int n) throws NoTransactionsException{
		return service.getTrans(n);
	}
	
	@GetMapping("/admin/getTrans/from/{date1}/to/{date2}")
	public List<Transactions> getTransBetweenDates(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
								LocalDateTime date1, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
								LocalDateTime date2) throws NoTransactionsException{
		return service.getTransByDate(date1,date2);
	}
	
	@GetMapping("/admin/getTrans/trtype/{type}")
	public List<Transactions> getTransByType(@PathVariable TransactionType type) throws NoTransactionsException{
		return service.getTransByType(type);
	}
	
	@GetMapping("/users/get/{acno}")
	public Users getDetails(@PathVariable long acno) throws UserNotFoundException {
		return service.getUser(acno);
	}
	
	@PutMapping("/users/update/{acno}")
	public Users updateDetails(@PathVariable long acno, @RequestBody Users user) {
		return service.updateDetails(acno, user);
	}
	
	@GetMapping("/users/{acno}/{n}")
	public List<Transactions> getTransactions(@PathVariable("acno") long acno, @PathVariable("n") int n) throws NoTransactionsException {
		return service.getTransactions(acno, n);
	}
	
	@PutMapping("/users/{acno}")
	public String depwit(@PathVariable long acno, @RequestBody Transactions tr) throws Exception {
		return service.depwit(acno, tr);
	}
	
	@GetMapping("/users/balance/{acno}")
	public String displayBal(@PathVariable long acno) {
		return service.displayBal(acno);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
