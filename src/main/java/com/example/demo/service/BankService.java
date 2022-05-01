package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.TransactionType;
import com.example.demo.entity.Transactions;
import com.example.demo.entity.Users;
import com.example.demo.error.InvalidException;
import com.example.demo.error.LowBalanceException;
import com.example.demo.error.NoTransactionsException;
import com.example.demo.error.UserNotFoundException;
import com.example.demo.repository.TransactionRepo;
import com.example.demo.repository.UserRepo;

@Service
public class BankService implements UserDetailsService{

	@Autowired
	private UserRepo repo;
	
	@Autowired
	private TransactionRepo trepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users user = repo.findByUsername(username);
		if(user==null)
			throw new UsernameNotFoundException("Username not found!!");
		return new BankPrincipal(user);
	}

	public Users saveUser(Users user) {
		String str = user.getPassword();
		user.setPassword(passwordEncoder.encode(str));
		return repo.save(user);
	}

	public String removeUser(Long acno) throws UserNotFoundException {
		if(!repo.existsByAcno(acno)) 
			throw new UserNotFoundException("Username not found!!");	
		else
			repo.deleteByAcno(acno);
		return "Record Deleted!";
			
	}

	public List<Users> getUsers() {
		return repo.findAll();
	}

	public List<Transactions> getTrans(Integer n) throws NoTransactionsException {
		List<Transactions> tr = trepo.findAllByOrderByTimeDesc();
		List<Transactions> res = new ArrayList<>();
		if(tr.size()==0)
			throw new NoTransactionsException("No Transactions Found");
		else {
			for(int i = 0; i < n && i < tr.size(); i++) {
				res.add(tr.get(i));
			}
			return res;
		}
//		List<Users> tr = repo.findAll();
//		List<Users> res = new ArrayList<>();
//		if(tr.size()==0)
//			throw new NoTransactionsException("No Transactions Found");
//		else {
//			for(int i = 0; i < n && i < tr.size(); i++) {
//				if(tr.get(i).getTransactions().size()!=0)
//					res.add(tr.get(i));
//			}
//			return res;
//		}
		
	}

	public Users updateUser(Long acno, Users user) throws UserNotFoundException {
		Users u;
		if(repo.existsByAcno(acno)) {
			u = repo.findByAcno(acno);
			if(Objects.nonNull(user.getName()) || !"".equals(user.getName()))
				u.setName(user.getName());
			if(Objects.nonNull(user.getAddress()) || !"".equals(user.getAddress()))
				u.setAddress(user.getAddress());
			if(Objects.nonNull(user.getCellno()) ||"".equals(user.getCellno()))
				u.setCellno(user.getCellno());
			if(Objects.nonNull(user.getEmail()) || !"".equals(user.getEmail()))
				u.setEmail(user.getEmail());
			return repo.save(u);
		}
		else
			throw new UserNotFoundException("Username not Found!!");
	}

	public Users getUser(Long acno) throws UserNotFoundException {
		if(!repo.existsByAcno(acno))
			throw new UserNotFoundException("User not found!");
		else
			return repo.findByAcno(acno);
	}

	public Users updateDetails(Long acno, Users user) {
		Users u = repo.findByAcno(acno);
		if(Objects.nonNull(user.getName()) || !"".equals(user.getName()))
			u.setName(user.getName());
		if(Objects.nonNull(user.getAddress()) || !"".equals(user.getAddress()))
			u.setAddress(user.getAddress());
		if(Objects.nonNull(user.getCellno()) ||"".equals(user.getCellno()))
			u.setCellno(user.getCellno());
		if(Objects.nonNull(user.getEmail()) || !"".equals(user.getEmail()))
			u.setEmail(user.getEmail());
		if(Objects.nonNull(user.getUsername()) || !"".equals(user.getUsername()))
			u.setUsername(user.getUsername());
		if(Objects.nonNull(user.getPassword()) || !"".equals(user.getPassword()))
			u.setPassword(passwordEncoder.encode(user.getPassword()));
		return repo.save(u);
	}

	public List<Transactions> getTransactions(Long acno, Integer n) throws NoTransactionsException {
		
		List<Transactions> tr =  trepo.findByUserAcnoOrderByTimeDesc(acno);
		List<Transactions> res = new ArrayList<>();
		if(tr.size()==0)
			throw new NoTransactionsException("No Transactions Found");
		else {
			for(int i = 0; i < n && i < tr.size(); i++) {
				res.add(tr.get(i));
			}
			return res;
		}
	}

	public String depwit(Long acno, Transactions tr) throws Exception {
		if(!repo.existsByAcno(acno))
			throw new UserNotFoundException("User not found");
		Users user = repo.findByAcno(acno);
		tr.setTime(LocalDateTime.now());
		tr.setUser(user);
		user.getTransactions().add(tr);
		double amount, amt;
		if(tr.getTrtype().equals(TransactionType.DEPOSIT)) {
			amount = user.getBalance() + tr.getAmount();
			user.setBalance(amount);
			trepo.save(tr);
		}
		else if(tr.getTrtype().equals(TransactionType.WITHDRAW)) {
			if(user.getBalance()<tr.getAmount()) {
				throw new LowBalanceException("Your account balance is low");
			}
			amount = user.getBalance() - tr.getAmount();
			user.setBalance(amount);
			trepo.save(tr);
		}
		else if(tr.getTrtype().equals(TransactionType.TRANSFER)){
			if(!repo.existsByAcno(tr.getRacno())) {
				throw new UserNotFoundException("Username not found");
			}
			else {
				Users reciever = repo.findByAcno(tr.getRacno());
				if(user.getBalance()<tr.getAmount()) {
					throw new LowBalanceException("Your account balance is low");
				}
				amount = user.getBalance() - tr.getAmount();
				user.setBalance(amount);
				amt = reciever.getBalance() + tr.getAmount();
				reciever.setBalance(amt);
				repo.save(reciever);
				trepo.save(tr);
			}
		}
		else {
			throw new InvalidException("Invalid transaction type");
		}
		return "Transaction Successful";
	}

	public String displayBal(Long acno) {
		Users user = repo.findByAcno(acno);
		return "Your account balance is Rs."+user.getBalance();
	}

	public List<Transactions> getTransByDate(LocalDateTime date1, LocalDateTime date2) {
		return trepo.findByTimeBetween(date1, date2);
	}

	public List<Transactions> getTransByType(TransactionType type) {
		return trepo.getTransactionsByTrtypeOrderByTimeDesc(type);
	}

	

	
}
