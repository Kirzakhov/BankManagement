package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class Transactions { 
	@Id
	@GeneratedValue
	private Integer id;
	@Column(columnDefinition = "bigint default 00")
	private Long racno = 0L;
	private TransactionType trtype;
	private Double amount;
	private LocalDateTime time;
	@ManyToOne
	@JoinColumn(name = "acno")
	private Users user;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public LocalDateTime getTime() {
		return time;
	}
	public void setTime(LocalDateTime time) {
		this.time = time;
	}
	public Long getRacno() {
		return racno;
	}
	public void setRacno(Long racno) {
		this.racno = racno;
	}
	public TransactionType getTrtype() {
		return trtype;
	}
	public void setTrtype(TransactionType trtype) {
		this.trtype = trtype;
	}
	public Users getUser() {
		return user;
	}
	public void setUser(Users user) {
		this.user = user;
	}
	@Override
	public String toString() {
		return "Transactions [id=" + id + ", racno=" + racno + ", trtype=" + trtype + ", amount=" + amount + ", time="
				+ time + ", user=" + user + "]";
	}
	
	
	
}
