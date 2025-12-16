package kr.sparta.livechat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * pring Security 설정을 담당하는 Configuration 클래스입니다.
 * 인증 및 인가 정책, 비밀번호 인코더, 보안 필터 체인 등의
 * 전반적인 보안 설정을 정의합니다.
 * SecurityConfig.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 * 비밀번호 암호화를 위한 설정입니다.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Spring Security 기본 보안 설정을 구성합니다.
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/auth/**").permitAll()
				.anyRequest().authenticated()
			);
		return http.build();
	}
}
