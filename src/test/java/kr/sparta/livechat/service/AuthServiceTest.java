package kr.sparta.livechat.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.sparta.livechat.dto.UserRegisterRequest;
import kr.sparta.livechat.dto.UserRegisterResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.UserRepository;

/**
 * AuthService 테스트 클래스입니다.
 * 외부 의존성 UserRepository, PasswordEncoder을 Mocking하여
 * 서비스 계층의 독립성을 보장합니다.
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

	private static final String VALID_EMAIL = "user@example.com";
	private static final String VALID_PASSWORD = "Password123!";
	private static final String ENCODED_PASSWORD = "encoded_password_hash";
	private static final String VALID_NAME = "TestUser";

	private UserRegisterRequest validRequest;
	private User savedUser;

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
	 * 유효한 요청 호출 시, 정상적으로 user가 저장되고
	 * 응답 객체가 올바르게 반환되는지 검증합니다.
	 */
	@Test
	@DisplayName("회원가입 요청 성공")
	void registerUser_SuccessTest() {
		// Given
		when(userRepository.existsByEmail(anyString())).thenReturn(false);

		// 1. 이메일 중복 체크
		when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn(ENCODED_PASSWORD);

		// 2. 비밀번호 암호화: 원본 -> 인코딩된 문자열 반환
		when(userRepository.save(any(User.class))).thenReturn(savedUser);

		// When
		UserRegisterResponse response = authService.registerUser(validRequest);

		// Then
		// 2. 응답 값 검증
		assertThat(response.getEmail()).isEqualTo(VALID_EMAIL);
		assertThat(response.getName()).isEqualTo(VALID_NAME);
		assertThat(response.getRole()).isEqualTo(Role.BUYER);

		// 3. 의존성 호출 검증
		verify(userRepository, times(1)).existsByEmail(VALID_EMAIL);
		verify(passwordEncoder, times(1)).encode(VALID_PASSWORD);
		verify(userRepository, times(1)).save(any(User.class));
	}

	/**
	 * 이미 존재하는 이메일로 호출 시,
	 * 예외코드 및 메세지 검증
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

		// 예외 코드 및 메시지 검증
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_DUPLICATE_EMAIL);

		// 불필요한 호출 검증
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any(User.class));
	}
}
