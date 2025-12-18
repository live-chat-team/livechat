package kr.sparta.livechat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import kr.sparta.livechat.config.JwtProperties;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;

/**
 * JwtService 클래스의 JWT 생성, 검증, 정보 추출 로직을 검증하는 단위 테스트 클래스이비다.
 * JwtProperties를 Mocking 하여 JWT 생성에 필요한 시크릿 키와 만료시간을 설정하며
 * JWT 라이브러리를 사용하여 토큰 생성 및 검증 성공, 실패 시나리오를 테스트 합니다.
 * JwtServiceTest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

	@InjectMocks
	private JwtService jwtService;

	@Mock
	private JwtProperties jwtProperties;

	private static final String TEST_SECRET = "c2VjcmV0a2V5Zm9ybGl2ZWNoYXRwcm9qZWN0MjU2Yml0";
	private static final Long TEST_USER_ID = 1L;
	private static final Role TEST_ROLE = Role.BUYER;
	private static final long ACCESS_EXP_MS = 3600000L;
	private static final long REFRESH_EXP_MS = 604800000L;

	private byte[] keyBytes;

	/**
	 * 각테스트 실행전에 Mock 객체를 초기화하고,
	 * JwtProperties의 값을 설정하여 Jwtservice r가 의존하는 값을 Mocking 합니다.
	 */
	@BeforeEach
	void setUp() {
		lenient().when(jwtProperties.getSecret()).thenReturn(TEST_SECRET);
		lenient().when(jwtProperties.getAccessTokenExpirationMs()).thenReturn(ACCESS_EXP_MS);
		lenient().when(jwtProperties.getRefreshTokenExpirationMs()).thenReturn(REFRESH_EXP_MS);
		try {
			keyBytes = Base64.getDecoder().decode(TEST_SECRET);
		} catch (IllegalArgumentException e) {
			keyBytes = TEST_SECRET.getBytes();
		}
	}

	/**
	 * Access Token 생성시 토큰이 null 이 아닌지 확인하고
	 * 토큰 내부에 사용자 id, 역할이 올바르게 포함되었는지 검증합니다.
	 */
	@Test
	@DisplayName("성공: Access Token 생성 및 정보 포함 확인")
	void createAccessToken_Success() {
		// When
		String token = jwtService.createAccessToken(TEST_USER_ID, TEST_ROLE);

		// Then
		assertNotNull(token);

		String decodedRole = Jwts.parserBuilder()
			.setSigningKey(Keys.hmacShaKeyFor(keyBytes))
			.build()
			.parseClaimsJws(token)
			.getBody()
			.get("role", String.class);

		Long decodedId = Jwts.parserBuilder()
			.setSigningKey(Keys.hmacShaKeyFor(keyBytes))
			.build()
			.parseClaimsJws(token)
			.getBody()
			.get("userId", Long.class);

		assertEquals(TEST_ROLE.name(), decodedRole);
		assertEquals(TEST_USER_ID, decodedId);
	}

	/**
	 * Refresh Token 생성시, 토큰이 null이 아닌지 확인하고
	 * 토큰 내부에 사용자 id는 포함되지만 역할은 포함되지 않는지 검증합니다.
	 */
	@Test
	@DisplayName("성공: Refresh Token 생성 및 정보 포함 확인")
	void createRefreshToken_Success() {
		// When
		String token = jwtService.createRefreshToken(TEST_USER_ID);

		// Then
		assertNotNull(token);
		String decodedRole = Jwts.parserBuilder()
			.setSigningKey(Keys.hmacShaKeyFor(keyBytes))
			.build()
			.parseClaimsJws(token)
			.getBody()
			.get("role", String.class);

		Long decodedId = Jwts.parserBuilder()
			.setSigningKey(Keys.hmacShaKeyFor(keyBytes))
			.build()
			.parseClaimsJws(token)
			.getBody()
			.get("userId", Long.class);

		assertNull(decodedRole);
		assertEquals(TEST_USER_ID, decodedId);
	}

	/**
	 * 윻요한 Access Token 을 검증했을대 예외가 발생하지 않고 종료되는지 확인합니다.
	 */
	@Test
	@DisplayName("성공: 유효한 토큰 검증 성공")
	void validateToken_Success() {
		// Given
		String validToken = jwtService.createAccessToken(TEST_USER_ID, TEST_ROLE);

		// When & Then
		assertDoesNotThrow(() -> jwtService.validateToken(validToken));
	}

	/**
	 * 만료된 토큰을 검증했을때 에러코드 예외가 발생하는지 확인하비다.
	 */
	@Test
	@DisplayName("실패: 만료된 토큰 검증 시 CustomException (AUTH_TOKEN_EXPIRED) 발생")
	void validateToken_Fail_Expired() {
		// Given
		String expiredToken = Jwts.builder()
			.claim("userId", TEST_USER_ID)
			.claim("role", TEST_ROLE.name())
			.setIssuedAt(Date.from(Instant.now().minusSeconds(10)))
			.setExpiration(Date.from(Instant.now().minusSeconds(5)))
			.signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
			.compact();

		// When & Then
		CustomException exception = assertThrows(CustomException.class,
			() -> jwtService.validateToken(expiredToken));

		assertEquals(ErrorCode.AUTH_TOKEN_EXPIRED, exception.getErrorCode());
	}

	/**
	 * 토큰 생서이 사용된 시크릿 키와 다른 키로 서명된 토큰을 검증했을때
	 * 에러코드 예외가 발생하느닞 확인합니다.
	 */
	@Test
	@DisplayName("실패:시크릿 토큰 검증 시 CustomException (AUTH_INVALID_TOKEN_FORMAT) 발생")
	void validateToken_Fail_InvalidSignature() {
		// Given

		String wrongSecret = "anothersecretkeyforlivechatprojectsecurity000000";
		byte[] wrongKeyBytes = wrongSecret.getBytes();

		String wrongSignedToken = Jwts.builder()
			.claim("userId", TEST_USER_ID)
			.setExpiration(Date.from(Instant.now().plusSeconds(3600)))
			.signWith(Keys.hmacShaKeyFor(wrongKeyBytes), SignatureAlgorithm.HS256)
			.compact();

		// When & Then
		CustomException exception = assertThrows(CustomException.class,
			() -> jwtService.validateToken(wrongSignedToken));
		assertEquals(ErrorCode.AUTH_INVALID_TOKEN_FORMAT, exception.getErrorCode());
	}

	/**
	 * 유요한 토큰에서 사용자 Id를 올바르게 추출하는지 확인합닏.
	 */
	@Test
	@DisplayName("성공: 토큰에서 사용자 ID 추출")
	void getUserIdFromToken_Success() {
		// Given
		String validToken = jwtService.createAccessToken(TEST_USER_ID, TEST_ROLE);

		// When
		Long extractedId = jwtService.getUserIdFromToken(validToken);

		// Then
		assertEquals(TEST_USER_ID, extractedId);
	}

	/**
	 * 유요한 토큰에서 만료시각을 올바르게 추출하는지 확인합니다.
	 */
	@Test
	@DisplayName("성공: 토큰에서 만료 시각 추출")
	void getExpirationFromToken_Success() {
		// Given
		final long futureExpirationTime = System.currentTimeMillis() + ACCESS_EXP_MS;

		String validToken = jwtService.createAccessToken(TEST_USER_ID, TEST_ROLE);

		// When
		Long extractedExp = jwtService.getExpirationFromToken(validToken);

		// Then
		assertTrue(extractedExp >= futureExpirationTime - 2000 &&
			extractedExp <= futureExpirationTime + 2000);
	}

	/**
	 * 이미 만료된 토큰이라도 마료시각 값 자체를 성공적으로 추출하는지 확인합니다,
	 */
	@Test
	@DisplayName("성공: 만료된 토큰에서 만료 시각 추출")
	void getExpirationFromToken_ExpiredToken_Success() {
		// Given
		final long pastExpirationTime = System.currentTimeMillis() - 5000;
		String expiredToken = Jwts.builder()
			.claim("userId", TEST_USER_ID)
			.setIssuedAt(Date.from(Instant.now().minusSeconds(10)))
			.setExpiration(Date.from(Instant.now().minusSeconds(5)))
			.signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
			.compact();

		// When
		Long extractedExp = jwtService.getExpirationFromToken(expiredToken);

		// Then
		assertNotNull(extractedExp);
		assertTrue(extractedExp < System.currentTimeMillis());
	}
}
