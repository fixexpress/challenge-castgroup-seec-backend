package com.castgroup.seec.challenge.controller;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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

		// setting default user its necessary in the first access to H2 in memory
		// database
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

	@PostMapping("/validatetoken")
	public ResponseEntity<?> validateToken(HttpServletRequest request) {

		final String authorizationHeader = request.getHeader("Authorization");

		String token = null;
		String username = null;
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			token = authorizationHeader.substring(7);
			username = jwtUtil.extractUsername(token);
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = this.userDatailService.loadUserByUsername(username);

			if (jwtUtil.validateToken(token, userDetails)) {
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}
		}

		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			// token is valid
			return ResponseEntity.ok("{validate:true}");
		} else {
			// token is invalid
			return ResponseEntity.badRequest().build();
		}

	}

	@PostMapping("/logoutinvalidatetoken")
	public ResponseEntity<?> logout(HttpServletRequest request) {
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			String token = authorizationHeader.substring(7);
			jwtUtil.invalidateToken(token);
		}
		return ResponseEntity.ok("{logout:true}");
	}
}
