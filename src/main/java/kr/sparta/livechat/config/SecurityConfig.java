package kr.sparta.livechat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import kr.sparta.livechat.security.JwtAuthenticationFilter;
import kr.sparta.livechat.security.JwtExceptionFilter;

/**
 * Spring Security 설정을 담당하는 Configuration 클래스입니다.
 * 인증 및 인가 정책, 비밀번호 인코더, 보안 필터 체인 등의
 * 전반적인 보안 설정을 정의합니다.
 * JwtAuthenticationFilter 와 JwtExceptionFilter 를 필터 체인에 등록하야
 * JWT 인증, 예외처리 담당하도록 하였습니다.
 * SecurityConfig.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 */
@Configuration
public class SecurityConfig {

	private final HandlerExceptionResolver handlerExceptionResolver;

	public SecurityConfig(HandlerExceptionResolver handlerExceptionResolver) {
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(
		HttpSecurity http,
		JwtAuthenticationFilter jwtAuthenticationFilter
	) throws Exception {

		http
			.csrf(csrf -> csrf.disable())
			.httpBasic(httpBasic -> httpBasic.disable())
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					HttpMethod.GET, "/api/products", "/api/products/*", "/api/products/**").permitAll()
				.requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
				.requestMatchers("/api/auth/logout").authenticated()
  				.requestMatchers("/api/admin/**").authenticated()
				.requestMatchers("/ws/**").permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(
				new JwtExceptionFilter(handlerExceptionResolver),
				UsernamePasswordAuthenticationFilter.class
			)
			.addFilterBefore(
				jwtAuthenticationFilter,
				UsernamePasswordAuthenticationFilter.class
			);

		return http.build();
	}
}
