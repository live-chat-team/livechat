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
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.global.exception.GlobalExceptionHandler;
import kr.sparta.livechat.service.AuthService;

/**
 * AuthController 단위 테스트
 * Controller 로직만 검증
 * Service는 Mock 처리
 * Validation / ExceptionHandler 동작 포함
 * AuthControllerTest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 12.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@InjectMocks
	private AuthController authController;

	@Mock
	private AuthService authService;

	private static final String REGISTER_URL = "/api/auth/register";
	private static final String VALID_EMAIL = "user@example.com";
	private static final String VALID_NAME = "TestUser";
	private static final String VALID_PASSWORD = "Password123!";

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		mockMvc = MockMvcBuilders
			.standaloneSetup(authController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	private UserRegisterRequest validRequest() {
		return new UserRegisterRequest(
			VALID_EMAIL,
			VALID_PASSWORD,
			VALID_NAME,
			Role.BUYER
		);
	}

	private UserRegisterResponse mockResponse() {
		return UserRegisterResponse.builder()
			.id(1L)
			.email(VALID_EMAIL)
			.name(VALID_NAME)
			.role(Role.BUYER)
			.createdAt(LocalDateTime.now())
			.build();
	}

	@Test
	@DisplayName("회원가입 성공 201 Created")
	void register_success() throws Exception {
		// given
		UserRegisterRequest request = validRequest();
		UserRegisterResponse response = mockResponse();

		when(authService.registerUser(any(UserRegisterRequest.class)))
			.thenReturn(response);

		// when & then
		mockMvc.perform(
				post(REGISTER_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value(VALID_EMAIL))
			.andExpect(jsonPath("$.name").value(VALID_NAME))
			.andExpect(jsonPath("$.role").value("BUYER"))
			.andDo(print());

		verify(authService, times(1)).registerUser(any(UserRegisterRequest.class));
	}

	@Test
	@DisplayName("회원가입 실패(ROLE ADMIN 시도")
	void register_fail_forbidden_admin_role() throws Exception {
		//given
		UserRegisterRequest request = new UserRegisterRequest(
			VALID_EMAIL,
			VALID_PASSWORD,
			VALID_NAME,
			Role.ADMIN
		);
		ErrorCode errorCode = ErrorCode.AUTH_FORBIDDEN_ROLE;
	}

	@Test
	@DisplayName("회원가입 실패 비밀번호 유효성 검사 실패 (400)")
	void register_fail_password_validation() throws Exception {
		// given
		UserRegisterRequest request = new UserRegisterRequest(
			VALID_EMAIL,
			"short",
			VALID_NAME,
			Role.BUYER
		);

		// when & then
		mockMvc.perform(
				post(REGISTER_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andDo(print());

		verify(authService, never()).registerUser(any());
	}

	@Test
	@DisplayName("회원가입 실패 이메일 중복 (CustomException)")
	void register_fail_duplicate_email() throws Exception {
		// given
		UserRegisterRequest request = validRequest();
		ErrorCode errorCode = ErrorCode.AUTH_DUPLICATE_EMAIL;

		doThrow(new CustomException(errorCode))
			.when(authService).registerUser(any(UserRegisterRequest.class));

		// when & then
		mockMvc.perform(
				post(REGISTER_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().is(errorCode.getStatus().value()))
			.andExpect(jsonPath("$.code").value(errorCode.getCode()))
			.andExpect(jsonPath("$.message").value(errorCode.getMessage()))
			.andDo(print());

		verify(authService, times(1)).registerUser(any(UserRegisterRequest.class));
	}
}
