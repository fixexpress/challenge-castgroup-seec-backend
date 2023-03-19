package com.castgroup.seec.challenge.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.castgroup.seec.challenge.dto.AuthRequest;
import com.castgroup.seec.challenge.dto.AuthResponse;
import com.castgroup.seec.challenge.model.User;
import com.castgroup.seec.challenge.security.JwtUtil;
import com.castgroup.seec.challenge.service.UserDetailService;

@RestController
public class AuthController {

	@Qualifier("authenticationManagerBean")
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserDetailService userDatailService;

	@Value("${spring.security.user.name}")
	private String defaultUserName;

	@Value("${spring.security.user.password}")
	private String defaultUserPassword;

	@PostMapping("/login")
	public ResponseEntity<?> authenticate(@RequestBody AuthRequest authRequest) {

		// setting default user its necessary in the first access to H2 in memory database
		Collection<User> colUser = userDatailService.findAll();
		if (colUser == null || colUser.isEmpty()) {
			User user = new User();
			user.setUsername(defaultUserName);
			user.setPassword(defaultUserPassword);
			userDatailService.save(user);
		}

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String jwt = jwtUtil.generateToken(userDetails);

		AuthResponse authResponse = new AuthResponse(jwt);

		return ResponseEntity.ok(authResponse);
	}
}
