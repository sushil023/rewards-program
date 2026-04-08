package com.rewards.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/*
 * Security configuration for the Rewards Program API.
 * Secures all API endpoints using HTTP Basic Authentication.
 * H2 console is accessible without authentication for development.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/*
	 * Configures HTTP security — which endpoints are protected.
	 *
	 * @param http the HttpSecurity object
	 * @return configured SecurityFilterChain
	 * @throws Exception if configuration fails
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				// Allow H2 console without login (development only)
				.requestMatchers("/h2-console/**").permitAll()
				.anyRequest().authenticated())
				// Enable HTTP Basic Auth (username/password in header)
				.httpBasic(httpBasic -> {
				})
				// Disable CSRF for REST API (stateless)
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
				// Allow H2 console frames
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

		return http.build();
	}

	/*
	 * Defines in-memory users for authentication. In production, replace with
	 * database-backed UserDetailsService.
	 *
	 * @param encoder the password encoder
	 * @return UserDetailsService with predefined users
	 */
	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder encoder) {
		
		// Admin user — full access
		UserDetails admin = User.builder().username("admin").password(encoder.encode("admin123")).roles("ADMIN")
				.build();

		// Regular user — read-only access
		UserDetails user = User.builder().username("user").password(encoder.encode("user123")).roles("USER").build();

		return new InMemoryUserDetailsManager(admin, user);
	}

	/*
	 * Password encoder using BCrypt hashing algorithm.
	 *
	 * @return BCryptPasswordEncoder instance
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
