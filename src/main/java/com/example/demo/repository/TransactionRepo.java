package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.TransactionType;
import com.example.demo.entity.Transactions;

@Repository
public interface TransactionRepo extends JpaRepository<Transactions, Integer> {
	
	List<Transactions> findAllByOrderByTimeDesc();
	List<Transactions> findByUserAcnoOrderByTimeDesc(Long acno);
	
	List<Transactions> findByTimeBetween(LocalDateTime date1, LocalDateTime date2);
	List<Transactions> getTransactionsByTrtypeOrderByTimeDesc(TransactionType type);
	
}
