package kr.sparta.livechat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import kr.sparta.livechat.dto.UserRegisterRequest;
import kr.sparta.livechat.dto.UserRegisterResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
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

	// Mock 객체를 주입받아 실제 인스턴스 생성
	@InjectMocks
	private AuthService authService;

	// DB 접근을 Mocking
	@Mock
	private UserRepository userRepository;

	// 비밀번호 암호화 로직을 Mocking
	@Mock
	private PasswordEncoder passwordEncoder;

	/**
	 * 테스트에 사용될 유저 등록 요청 객체를 생성하는 헬퍼 메서드입니다.
	 * @return UserRegisterRequest 객체
	 */
	private UserRegisterRequest createRegisterRequest() {
		return new UserRegisterRequest(
			"test@example.com",
			"Password123!",
			"Tester",
			Role.BUYER
		);
	}

	//  1. 회원가입 성공 테스트

	/**
	 * 유효한 요청 호출 시, 정상적으로 user가 저장되고
	 * 응답 객체가 올바르게 반환되는지 검증합니다.
	 */
	@Test
	@DisplayName("registerUser_SuccessTest")
	void registerUser_SuccessTest() throws Exception {
		// Given
		UserRegisterRequest request = createRegisterRequest();
		String rawPassword = request.getPassword();
		String encodedPassword = "encodedPassword123";

		// 1. 이메일 중복 체크
		when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

		// 2. 비밀번호 암호화: 원본 -> 인코딩된 문자열 반환
		when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

		// 3. UserRepository.save() Mocking

		/**
		 * DB에 저장되는 시점에 ID가 할당되는 것을 Mockito의 doAnswer로 시뮬레이션합니다.
		 * Reflection을 사용하여 Mock User 객체의 private ID 필드에 1L을 강제로 주입합니다.
		 */
		doAnswer(new Answer<User>() {
			@Override
			public User answer(InvocationOnMock invocation) throws Throwable {
				User user = invocation.getArgument(0);

				// Reflection을 사용하여 private id 필드에 접근 및 값 설정
				java.lang.reflect.Field idField = User.class.getDeclaredField("id");
				idField.setAccessible(true);
				idField.set(user, 1L);

				return user;
			}
		}).when(userRepository).save(any(User.class));

		// When
		UserRegisterResponse response = authService.registerUser(request);

		// Then
		assertNotNull(response);
		// Assertion: DB 저장 후 ID가 올바르게 응답에 포함되었는지 검증
		assertEquals(1L, response.getId());
		assertEquals(request.getEmail(), response.getEmail());
		assertEquals(request.getName(), response.getName());
		assertEquals(request.getRole(), response.getRole());

		// Assertion: 핵심 메서드 호출 횟수 검증
		verify(userRepository, times(1)).existsByEmail(request.getEmail());
		verify(passwordEncoder, times(1)).encode(rawPassword);
		verify(userRepository, times(1)).save(any(User.class));
	}

	// 2. 회원가입 실패 테스트 (이메일 중복)

	/**
	 * 이미 존재하는 이메일로 호출 시,
	 * userRepository.existsByEmail() 에서 true를 반환하고
	 * 409 Conflict 예외가 발생하는지 검증합니다.
	 */
	@Test
	@DisplayName("registerUser_Fail_DuplicateEmailTest")
	void registerUser_Fail_DuplicateEmailTest() {
		// Given
		UserRegisterRequest request = createRegisterRequest();

		// Mocking: 이메일 중복 체크 시 true 반환 설정
		when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

		// When & Then
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			authService.registerUser(request);
		});

		// Assertion: 예외의 상태 코드 및 메시지 확인 (409 Conflict)
		assertEquals(409, exception.getStatusCode().value());
		assertEquals("이미 사용 중인 이메일 주소입니다.", exception.getReason());

		// Assertion: 중복 체크 후 로직이 중단되었으므로, save와 encode는 호출되지 않았어야 함
		verify(userRepository, times(1)).existsByEmail(request.getEmail());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any(User.class));
	}

	// 3. 회원가입 실패 테스트 (ADMIN 역할 등록 차단)

	/**
	 * ADMIN 역할로 회원가입 요청 시,
	 * 일반 API를 통한 등록이 차단되고 403 Forbidden 예외가 발생하는지 검증합니다.
	 */
	@Test
	@DisplayName("registerUser_Fail_AdminRoleNotAllowedTest")
	void registerUser_Fail_AdminRoleNotAllowedTest() {
		// Given
		// ADMIN 역할이 포함된 요청 준비
		UserRegisterRequest adminRequest = new UserRegisterRequest(
			"admin@example.com",
			"AdminPassword123!",
			"Admin",
			Role.ADMIN
		);
		when(userRepository.existsByEmail(adminRequest.getEmail())).thenReturn(false);

		// When & Then (실행 및 예외 검증)
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			authService.registerUser(adminRequest);
		});

		// Assertion: 예외의 상태 코드 및 메시지 확인 (403 Forbidden)
		assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
		assertEquals("ADMIN 역할은 일반 회원가입을 통해 등록할 수 없습니다.", exception.getReason());

		// Assertion: ADMIN 역할 요청은 차단되었으므로, save와 encode는 호출되지 않았어야 함
		verify(userRepository, times(1)).existsByEmail(adminRequest.getEmail());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any(User.class));
	}
}