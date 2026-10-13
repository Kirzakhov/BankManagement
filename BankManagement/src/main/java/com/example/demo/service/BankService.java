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
import org.springframework.transaction.annotation.Transactional;

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

	private List<Transactions> limitTransactions(List<Transactions> transactions, Integer n) throws NoTransactionsException {
		if (transactions == null || transactions.isEmpty()) {
			throw new NoTransactionsException("No Transactions Found");
		}
		List<Transactions> res = new ArrayList<>();
		int limit = (n == null || n < 0) ? 0 : n;
		for (int i = 0; i < limit && i < transactions.size(); i++) {
			res.add(transactions.get(i));
		}
		return res;
	}

	public List<Transactions> getTrans(Integer n) throws NoTransactionsException {
		return limitTransactions(trepo.findAllByOrderByTimeDesc(), n);
	}

	private void applyUserFields(Users u, Users user) {
		if(Objects.nonNull(user.getName()) && !user.getName().isEmpty())
			u.setName(user.getName());
		if(Objects.nonNull(user.getAddress()) && !user.getAddress().isEmpty())
			u.setAddress(user.getAddress());
		if(Objects.nonNull(user.getCellno()) && !user.getCellno().isEmpty())
			u.setCellno(user.getCellno());
		if(Objects.nonNull(user.getEmail()) && !user.getEmail().isEmpty())
			u.setEmail(user.getEmail());
	}

	public Users updateUser(Long acno, Users user) throws UserNotFoundException {
		if(!repo.existsByAcno(acno))
			throw new UserNotFoundException("Username not Found!!");
		Users u = repo.findByAcno(acno);
		applyUserFields(u, user);
		return repo.save(Objects.requireNonNull(u));
	}

	public Users getUser(Long acno) throws UserNotFoundException {
		if(!repo.existsByAcno(acno))
			throw new UserNotFoundException("User not found!");
		else
			return repo.findByAcno(acno);
	}

	public boolean isAccountOwner(Long acno, String username) {
		Users user = repo.findByUsername(username);
		return user != null && Objects.equals(user.getAcno(), acno);
	}

	public Users updateDetails(Long acno, Users user) {
		Users u = repo.findByAcno(acno);
		applyUserFields(u, user);
		if(Objects.nonNull(user.getUsername()) && !user.getUsername().isEmpty())
			u.setUsername(user.getUsername());
		if(Objects.nonNull(user.getPassword()) && !user.getPassword().isEmpty())
			u.setPassword(passwordEncoder.encode(user.getPassword()));
		return repo.save(Objects.requireNonNull(u));
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

	@Transactional(rollbackFor = Exception.class)
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

				Transactions received = new Transactions();
				received.setAmount(tr.getAmount());
				received.setTrtype(TransactionType.TRANSFER_RECEIVED);
				received.setRacno(user.getAcno());
				received.setTime(tr.getTime());
				received.setUser(reciever);
				reciever.getTransactions().add(received);
				trepo.save(received);
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
