package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.AuthDtos;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.UserRepository;
import com.expensetracker.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager,
			JwtService jwtService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Transactional
	public void register(AuthDtos.RegisterRequest req) {
		if (userRepository.existsByEmail(req.getEmail())) {
			throw new IllegalArgumentException("Email already registered");
		}

		User user = User.builder()
				.name(req.getName())
				.email(req.getEmail())
				.password(passwordEncoder.encode(req.getPassword()))
				.build();

		userRepository.save(user);
	}

	public String loginAndGetToken(AuthDtos.LoginRequest req) {
		try {
			var authToken = new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());
			authenticationManager.authenticate(authToken);
			return jwtService.generateToken(req.getEmail());
		} catch (Exception e) {
			throw new BadCredentialsException("Invalid email or password");
		}
	}
}

