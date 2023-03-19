package com.castgroup.seec.challenge.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.castgroup.seec.challenge.model.User;
import com.castgroup.seec.challenge.repository.UserRepository;
import com.castgroup.seec.challenge.security.CustomPasswordEncoder;

@Service
public class UserService implements UserDetailsService {

	private UserRepository userRepository;
	private CustomPasswordEncoder customPasswordEncoder;

	@Autowired
	public UserService(UserRepository userRepository, @Lazy CustomPasswordEncoder customPasswordEncoder) {
		this.userRepository = userRepository;
		this.customPasswordEncoder = customPasswordEncoder;

	}

	public void registerUser(User user) {
		User newUser = new User();
		newUser.setId(user.getId());
		newUser.setUsername(user.getUsername());
		newUser.setPassword(customPasswordEncoder.encode(user.getPassword()));
		userRepository.save(newUser);

	}

	public User findUserById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new NullPointerException("user not found"));
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User userEntity = userRepository.findByUsername(username);
		if (userEntity == null)
			throw new UsernameNotFoundException(username);
		return new org.springframework.security.core.userdetails.User(username, null, Collections.emptyList());

	}
}