package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepo;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class BankConfig {

	private static final Logger log = LoggerFactory.getLogger(BankConfig.class);
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public ApplicationRunner bootstrapAdmin(UserRepo repo, PasswordEncoder encoder,
			@Value("${BANK_ADMIN_PASSWORD:}") String initialPassword) {
		return args -> {
			if (repo.findByUsername("admin") != null) {
				return;
			}
			if (initialPassword.isBlank()) {
				log.warn("No administrator account exists. Set BANK_ADMIN_PASSWORD to create one on startup.");
				return;
			}

			Users admin = new Users();
			admin.setName("System Administrator");
			admin.setUsername("admin");
			admin.setPassword(encoder.encode(initialPassword));
			repo.save(admin);
			log.info("Created the initial administrator account from BANK_ADMIN_PASSWORD.");
		};
	}
	
	@Bean
	public AuthenticationProvider authProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(encoder());
		return provider;
	}
	

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf.disable())
			.headers(headers -> headers.frameOptions(frame -> frame.disable()))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/").permitAll()
				.requestMatchers("/h2-console/**").permitAll()
				.requestMatchers("/users/**").hasAuthority("USER")
				.requestMatchers("/admin/**").hasAuthority("ADMIN")
				.anyRequest().authenticated())
			.httpBasic(Customizer.withDefaults());
		return http.build();
	}
	
	

}
