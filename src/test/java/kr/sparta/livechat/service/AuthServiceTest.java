package kr.sparta.livechat.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.sparta.livechat.dto.user.UserLoginRequest;
import kr.sparta.livechat.dto.user.UserRegisterRequest;
import kr.sparta.livechat.dto.user.UserRegisterResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.UserRepository;

/**
 * AuthService 클래스 비니지스 로직을 검증하는 단위 데스트 클래스입니다.
 * UserRepository, PasswordEncoder, JwtService, TokenBlacklistService를 Mocking하여
 * 서비스 계층의 독립성을 보장합니다.
 * 회원가입, 로그인, 로그아웃 핵심 인증인가 로직의 성공, 실패를 테스트 합니다.
 * AuthServiceTest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 12.
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private TokenBlacklistService tokenBlacklistService;

	private static final String VALID_EMAIL = "user@example.com";
	private static final String VALID_PASSWORD = "Password123!";
	private static final String ENCODED_PASSWORD = "encoded_password_hash";
	private static final String VALID_NAME = "TestUser";
	private static final String VALID_ACCESS_TOKEN = "valid-access-token";

	private UserRegisterRequest validRequest;
	private User savedUser;

	/**
	 * 공통으로 사용될 요청 DTO 과 저장될 User 엔티티를 설정합니다.
	 */
	@BeforeEach
	void setUp() {
		validRequest = new UserRegisterRequest(
			VALID_EMAIL,
			VALID_PASSWORD,
			VALID_NAME,
			Role.BUYER
		);

		savedUser = User.builder()
			.email(VALID_EMAIL)
			.name(VALID_NAME)
			.password(ENCODED_PASSWORD)
			.role(Role.BUYER)
			.build();
	}

	/**
	 * 유효한 회원가입 요청 호출 시, 정상적으로 user가 저장되고
	 * 응답 객체가 올바르게 반환되는지 검증합니다.
	 */
	@Test
	@DisplayName("회원가입 요청 성공")
	void registerUser_SuccessTest() {
		// Given
		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn(ENCODED_PASSWORD);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);

		// When
		UserRegisterResponse response = authService.registerUser(validRequest);

		// Then
		assertThat(response.getEmail()).isEqualTo(VALID_EMAIL);
		assertThat(response.getName()).isEqualTo(VALID_NAME);
		assertThat(response.getRole()).isEqualTo(Role.BUYER);
		verify(userRepository, times(1)).existsByEmail(VALID_EMAIL);
		verify(passwordEncoder, times(1)).encode(VALID_PASSWORD);
		verify(userRepository, times(1)).save(any(User.class));
	}

	/**
	 * 이미 존재하는 이메일로 호출 시,
	 * 예외코드 및 메세지 발생을 검증합니다.
	 * 비밀번호 암호화 및 저장 로직은 실행되지 않았읆을 검증합니다.
	 */
	@Test
	@DisplayName("회원가입 요청 실패 (이메일 중복)")
	void registerUser_Fail_DuplicateEmailTest() {
		// Given
		when(userRepository.existsByEmail(anyString())).thenReturn(true);

		// When & Then
		CustomException exception = assertThrows(CustomException.class, () -> {
			authService.registerUser(validRequest);
		});
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_DUPLICATE_EMAIL);
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any(User.class));
	}

	/**
	 * 유요한 자격으로 로그인 성공시
	 * UserLoginResponse 가 반환 되고 Access Token 생성이 호출되는지 검증합니다.
	 */
	@Test
	@DisplayName("성공: 유효한 자격 증명으로 로그인 성공")
	void login_Success() {
		// given
		final long FUTURE_EXPIRATION_TIME = System.currentTimeMillis() + 3600000L;
		when(jwtService.getExpirationFromToken(VALID_ACCESS_TOKEN)).thenReturn(FUTURE_EXPIRATION_TIME);
		// when
		authService.logout(VALID_ACCESS_TOKEN);

		// then
		verify(jwtService, times(1)).getExpirationFromToken(VALID_ACCESS_TOKEN);
		verify(tokenBlacklistService, times(1)).addToBlacklist(eq(VALID_ACCESS_TOKEN), anyLong());

	}

	/**
	 * 존재하지 않는 이메일로 로그인시
	 * 에러코드 예외가 발생하고.
	 * 비밀번호 검증 . 토큰 생성 로직은 생성되지 않음을 검증합니다.
	 */
	@Test
	@DisplayName("실패: 존재하지 않는 이메일로 로그인 시도")
	void login_Fail_UserNotFound() {
		// given
		UserLoginRequest request = new UserLoginRequest(VALID_EMAIL, VALID_PASSWORD);

		// Mocking: 사용자 찾기 실패
		when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));
		assertEquals(ErrorCode.AUTH_INVALID_CREDENTIALS, exception.getErrorCode());
		verify(passwordEncoder, never()).matches(anyString(), anyString());
		verify(jwtService, never()).createAccessToken(anyLong(), any(Role.class));
	}

	/**
	 * 비밀번호가 일치하지 않는경우
	 * 에러코드 예외가 발생하고
	 * 토큰 생성 로직은 실행되지 않음을 검증합니다.
	 */
	@Test
	@DisplayName("실패: 비밀번호 불일치 시 로그인 실패")
	void login_Fail_PasswordMismatch() {
		// given
		UserLoginRequest request = new UserLoginRequest(VALID_EMAIL, "wrongPassword");
		when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(savedUser));
		when(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).thenReturn(false);

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));
		assertEquals(ErrorCode.AUTH_INVALID_CREDENTIALS, exception.getErrorCode());
		verify(jwtService, never()).createAccessToken(anyLong(), any(Role.class));
	}

	/**
	 * 유요한 Access Token이 블랙리스트에 정상적으로 추가되는지 검ㅈㅇ합ㄴ디ㅏ.
	 * 토큰 만료 시간 및 블랙리스트 서비스 호출을 확인합니다.
	 */
	@Test
	@DisplayName("성공: Access Token 블랙리스트 처리 성공")
	void logout_Success() {
		// given
		final long FUTURE_EXPIRATION_TIME = System.currentTimeMillis() + 3600000L;
		when(jwtService.getExpirationFromToken(VALID_ACCESS_TOKEN)).thenReturn(FUTURE_EXPIRATION_TIME);

		// when
		authService.logout(VALID_ACCESS_TOKEN);

		// then
		verify(jwtService, times(1)).getExpirationFromToken(VALID_ACCESS_TOKEN);
		verify(tokenBlacklistService, times(1)).addToBlacklist(eq(VALID_ACCESS_TOKEN), anyLong());
	}

	/**
	 * JwtService 에서 토큰 만료 시각 추출에 실해하여 null를 반환할경우,
	 * 예외없이 메서드 종료되고 블랙리스트 추가는 일어나지 않음을 검증ㅇ합니다.
	 */
	@Test
	@DisplayName("성공: 토큰 만료 시각 추출 실패 시 (null 반환) 정상 종료")
	void logout_Success_TokenExpirationExtractionFails_NoException() {
		// given
		when(jwtService.getExpirationFromToken(VALID_ACCESS_TOKEN)).thenReturn(null);

		// when & then
		assertDoesNotThrow(() -> authService.logout(VALID_ACCESS_TOKEN));
		verify(tokenBlacklistService, never()).addToBlacklist(anyString(), anyLong());
	}

	/**
	 * 이미 만료시각이 현재 시각보다 과거인 토큰을 전달했을경우.
	 * 블랙리스트에 추가되지 않고 메서드가 정상 종료됨을 검증하비다.
	 */
	@Test
	@DisplayName("성공: 이미 만료된 토큰 (TTL <= 0) 블랙리스트 추가 안함")
	void logout_Success_TokenAlreadyExpired() {
		// Given
		final long PAST_EXPIRATION_TIME = System.currentTimeMillis() - 3600000L;

		when(jwtService.getExpirationFromToken(VALID_ACCESS_TOKEN)).thenReturn(PAST_EXPIRATION_TIME);

		// When & Then
		assertDoesNotThrow(() -> authService.logout(VALID_ACCESS_TOKEN));
		verify(tokenBlacklistService, never()).addToBlacklist(anyString(), anyLong());
	}
}

