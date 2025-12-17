package kr.sparta.livechat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 관련 설정 값을 관리 하는 클래스입니다
 * yml 파일에서 jwt 로 정의 된 속성을 주입 받아 사용합니다.
 * JwtProperties.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
	private String secret;
	private long accessTokenExpirationMs;
	private long refreshTokenExpirationMs;
}
