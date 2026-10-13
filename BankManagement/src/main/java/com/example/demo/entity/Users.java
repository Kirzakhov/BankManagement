package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Email;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@SequenceGenerator(name="seq", initialValue=10000000)
public class Users {
	
	@Length(min=3, message="Name cannot be less than three characters")
	private String name;
	
	@Id
	@Column(unique = true)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="seq")
	private Long acno;
	
	@Column(unique = true)
	@Length(min=10, max = 13, message="Cell number should not be less than 10 and greater than 13 chcaracters")
	private String cellno;
	
	@Email
	@Column(unique = true)
	private String email;
	
	@Length(min=10, message="Address cannot be less than ten characters")
	private String address;
	
	@Column(unique = true)
	@Length(min=10, max=10, message="PAN should be of ten characters")
	private String pan;
	
	@Length(min=12, max=12, message="Aadhar number must be of 12 characters")
	@Column(unique = true)
	private String adhaar;

	@Column(columnDefinition = "double default 10000")
	private Double balance = 10000.00;
	
	@Column(unique = true)
	private String username;
	
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "acno")
	@JsonIgnore
	private List<Transactions> transactions = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getAcno() {
		return acno;
	}
	public void setAcno(Long acno) {
		this.acno = acno;
	}
	public String getCellno() {
		return cellno;
	}
	public void setCellno(String cellno) {
		this.cellno = cellno;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPan() {
		return pan;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	public String getAdhaar() {
		return adhaar;
	}
	public void setAdhaar(String adhaar) {
		this.adhaar = adhaar;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<Transactions> getTransactions() {
		return transactions;
	}
	public void setTransactions(List<Transactions> transactions) {
		this.transactions = transactions;
	}
	@Override
	public String toString() {
		return "Users [name=" + name + ", acno=" + acno + ", cellno=" + cellno + ", email=" + email
				+ ", address=" + address + ", pan=" + pan + ", adhaar=" + adhaar + ", balance=" + balance
				+ ", username=" + username + ", password=" + password + "]";
	}
	
}
