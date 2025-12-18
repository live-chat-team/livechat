package kr.sparta.livechat.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import kr.sparta.livechat.config.JwtProperties;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 생성, 유효성 검증을 담당하는 서비스 클래스입니다.
 * JwtProperties 에 정의된 Secret Key와 만료 시간을 사용하여 토큰 작업을 수행합니다.
 * JwtService.java
 *
 * @author kimsehyu
 * @since 2025. 12. 16.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtProperties jwtProperties;

	/**
	 * 사용자 ID와 역할을 포함하여 Access Token을 생성합니다
	 * @param userId 토큰에 포함될 사용자 ID
	 * @param role 토큰에 포함될 사용자 역할
	 * @return 생성된 Access Token
	 */
	public String createAccessToken(Long userId, Role role) {
		return createToken(userId, role, jwtProperties.getAccessTokenExpirationMs());
	}

	/**
	 * 사용자 ID만 포함하는 Refresh Token을 생성합니다.
	 * @param userId 토큰에 포함될 사용자 ID
	 * @return 생성된 Refresh Token
	 */
	public String createRefreshToken(Long userId) {
		return createToken(userId, null, jwtProperties.getRefreshTokenExpirationMs());
	}

	/**
	 * userID, role, expirationTime 을 바탕으로 JWT를 생성합니다.
	 * @param userId 포함될 사용자 ID
	 * @param role 포함될 사용자 역할
	 * @param expirationTime 토큰의 유효시간
	 * @return JWT 문자열
	 */
	private String createToken(Long userId, Role role, long expirationTime) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", userId);
		if (role != null) {
			claims.put("role", role.name());
		}

		Date now = new Date();
		Date expiration = new Date(now.getTime() + expirationTime);
		return Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(now)
			.setExpiration(expiration)
			.signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
			.compact();
	}

	/**
	 * 토큰의 유효성을 검사합니다
	 * 키 , 만료시간 확인 JWT에 따른 검증을 수행,
	 * 실패시 CustomException
	 * @param token 유효성을 검증할 JWT 문자열
	 * @return 토큰이 유효하면 true
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token);
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.warn("잘못된 JWT 서명 또는 형식 오류입니다. {}", e.getMessage());
			throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN_FORMAT);
		} catch (ExpiredJwtException e) {
			log.warn("만료된 JWT 토큰입니다. {}", e.getMessage());
			throw new CustomException(ErrorCode.AUTH_TOKEN_EXPIRED);
		} catch (UnsupportedJwtException e) {
			log.warn("지원되지 않는 JWT 토큰입니다. {}", e.getMessage());
			throw new CustomException(ErrorCode.AUTH_TOKEN_UNSUPPORTED);
		} catch (IllegalArgumentException e) {
			log.warn("JWT 토큰이 잘못되었습니다. {}", e.getMessage());
			throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN_FORMAT);
		}
	}

	/**
	 * 우요한 토큰에서 userId 를 추출합니다
	 * @param token 추출할 JWT 문자열
	 * @return 추출된 사용자 ID
	 */
	public Long getUserIdFromToken(String token) {
		Claims claims = Jwts.parser()
			.setSigningKey(jwtProperties.getSecret())
			.parseClaimsJws(token)
			.getBody();
		return claims.get("userId", Long.class);
	}

	/**
	 * 토큰에서 만료시각을 추출합니다.
	 * 만료된 토큰인 경우에도 예외를 처리하고 만료시각 반환
	 * 로그아웃시 토큰의 남은 TTL 계산
	 * @param token 만료 시각을 추출할 JWT
	 * @return 만료 시각 추출 실패시 null
	 */
	public Long getExpirationFromToken(String token) {
		try {
			Claims claims = Jwts.parser()
				.setSigningKey(jwtProperties.getSecret())
				.parseClaimsJws(token)
				.getBody();
			return claims.getExpiration().getTime();
		} catch (ExpiredJwtException e) {
			return e.getClaims().getExpiration().getTime();
		} catch (Exception e) {
			log.warn("토큰 만료 시각 추출 실패: {}", e.getMessage());
			return null;
		}
	}
}
