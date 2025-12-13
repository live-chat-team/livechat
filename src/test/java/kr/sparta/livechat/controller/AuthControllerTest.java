package kr.sparta.livechat.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import kr.sparta.livechat.dto.UserRegisterRequest;
import kr.sparta.livechat.dto.UserRegisterResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.exception.GlobalExceptionHandler;
import kr.sparta.livechat.service.AuthService;

/**
 * AuthController 테스트 클래스입니다.
 * MockMvcBuilders.standaloneSetup을 사용하여 Controller와 GlobalExceptionHandler를
 * 직접 설정하고, Service 계층을 Mocking하여 Controller의 독립적인 동작을 검증합니다.
 * AuthControllerTest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 12.
 */
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks
	private AuthController authController;

	@Mock
	private AuthService authService;

	/**
	 * 각 테스트 실행 전 MockMvc 환경을 설정합니다.
	 * MockMvcBuilders.standaloneSetup을 통해 Controller와 GlobalExceptionHandler를
	 * 수동으로 등록하여 예외 처리 로직의 검증이 가능하도록 합니다.
	 */
	@BeforeEach
	void setUp() {
		objectMapper.registerModule(new JavaTimeModule());

		this.mockMvc = MockMvcBuilders.standaloneSetup(authController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	/**
	 * Mock 응답 객체를 생성하는 헬퍼 메서드입니다.
	 * @return UserRegisterResponse
	 */
	private UserRegisterResponse createMockResponse() {
		return UserRegisterResponse.builder()
			.id(1L)
			.email("test@example.com")
			.name("Tester")
			.role(Role.BUYER)
			.createdAt(LocalDateTime.now())
			.build();
	}

	//1. 회원가입 성공 테스트

	/**
	 * 유효한 회원가입 요청 시, AuthService 호출 후 201 Created 응답을 검증합니다.
	 * @throws Exception MockMvc 수행 중 발생 가능한 예외
	 */
	@Test
	@DisplayName("회원가입 요청_성공 (201 Created) Test")
	void registerUser_SuccessTest() throws Exception {
		// Given
		UserRegisterRequest request = new UserRegisterRequest(
			"test@example.com",
			"Password123!",
			"Tester",
			Role.BUYER
		);
		String json = objectMapper.writeValueAsString(request);
		UserRegisterResponse mockResponse = createMockResponse();

		// Mocking: Service가 성공적으로 처리한다고 가정
		when(authService.registerUser(any(UserRegisterRequest.class))).thenReturn(mockResponse);

		// When
		// Then
		mockMvc.perform(
				post("/api/auth/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andExpect(status().isCreated())
			.andDo(print());

		verify(authService, times(1)).registerUser(any(UserRegisterRequest.class));
	}

	//2. 유효성 검사 실패 테스트

	/**
	 * 유효성 검사 조건을 위반할 경우, 400 Bad Request 응답을 검증합니다.
	 * 이 테스트는 Service가 호출되기 전에 Controller 레벨에서 실패함을 검증합니다.
	 */
	@Test
	@DisplayName("회원가입 요청_실패 (400 Bad Request: 비밀번호 유효성 위반) Test")
	void registerUser_Fail_Validation_PasswordTest() throws Exception {
		// Given
		UserRegisterRequest invalidRequest = new UserRegisterRequest(
			"test@example.com",
			"short",
			"Tester",
			Role.SELLER
		);
		String json = objectMapper.writeValueAsString(invalidRequest);

		// When
		// Then
		mockMvc.perform(
				post("/api/auth/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andExpect(status().isBadRequest())
			.andDo(print());

		verify(authService, never()).registerUser(any(UserRegisterRequest.class));
	}

	// 3. Service 예외 처리 테스트

	/**
	 * Service 계층에서 이메일 중복 발생 시,
	 * GlobalExceptionHandler가 이를 처리하여 400 Bad Request를 반환하는지 검증합니다.
	 */
	@Test
	@DisplayName("회원가입 요청_실패 (400 Bad Request: 이메일 중복) Test")
	void registerUser_Fail_Service_DuplicateEmailTest() throws Exception {
		// Given
		UserRegisterRequest request = new UserRegisterRequest(
			"duplicate@example.com",
			"Password123!",
			"Tester",
			Role.BUYER
		);
		String json = objectMapper.writeValueAsString(request);

		// Mocking: Service 호출 시 IllegalArgumentException 예외 발생 설정
		doThrow(new IllegalArgumentException("이미 존재하는 이메일입니다."))
			.when(authService).registerUser(any(UserRegisterRequest.class));

		// When
		// Then
		mockMvc.perform(
				post("/api/auth/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
			)
			.andExpect(status().isBadRequest())
			.andDo(print());

		verify(authService, times(1)).registerUser(any(UserRegisterRequest.class));
	}
}