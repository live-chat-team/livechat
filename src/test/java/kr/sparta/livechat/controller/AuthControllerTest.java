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

import kr.sparta.livechat.dto.UserLoginRequest;
import kr.sparta.livechat.dto.UserLoginResponse;
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
	private static final String LOGIN_URL = "/api/auth/login";
	private static final String LOGOUT_URL = "/api/auth/logout";
	private static final String VALID_EMAIL = "user@example.com";
	private static final String VALID_NAME = "TestUser";
	private static final String VALID_PASSWORD = "Password123!";
	private static final String VALID_ACCESS_TOKEN = "valid.jwt.token";

	/**
	 * 각테스트 실행전 MockMvc 객체 생성합니다.
	 */
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

	/**
	 * 유요한 요청으로 회원가입 시도하여 201 응답을 확인하빈다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
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

	/**
	 * 클라이언트가 ADMIN으로 회원가입시 예외를 확인합니다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
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

	/**
	 * 비밀번호 유효성 검사 규칙을 만족하지 않았을때 400 응답을 확인합니다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
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

	/**
	 * service 에서 이메일 중복CustomException 을 던질경우
	 * GlobalExceptionHandler 통해 409 응답이 오는지 확인합니다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
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

	/**
	 * 유요한 요청으로 로그인 시도시 200 OK 응답과 토큰 응답 DTO를 확인합니다,
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("성공: 로그인 성공 시 200 OK와 토큰 반환")
	void login_success() throws Exception {
		// given
		UserLoginRequest request = new UserLoginRequest(VALID_EMAIL, VALID_PASSWORD);
		UserLoginResponse mockResponse = UserLoginResponse.builder()
			.accessToken(VALID_ACCESS_TOKEN)
			.refreshToken(null)
			.build();

		when(authService.login(any(UserLoginRequest.class)))
			.thenReturn(mockResponse);

		// when & then
		mockMvc.perform(
				post(LOGIN_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value(VALID_ACCESS_TOKEN))
			.andDo(print());

		verify(authService, times(1)).login(any(UserLoginRequest.class));
	}

	/**
	 * Service 에서 인증 정보 불일치시 CustomException 던질경우
	 * GlobalExceptionHandler를 통해 401 응답이 오는지 확인합니다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("실패: 로그인 시 인증 정보 불일치 (401 Unauthorized)")
	void login_fail_invalid_credentials() throws Exception {
		// given
		UserLoginRequest request = new UserLoginRequest(VALID_EMAIL, "wrongpassword");
		ErrorCode errorCode = ErrorCode.AUTH_INVALID_CREDENTIALS;

		doThrow(new CustomException(errorCode))
			.when(authService).login(any(UserLoginRequest.class));

		// when & then
		mockMvc.perform(
				post(LOGIN_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(errorCode.getCode()))
			.andDo(print());

		verify(authService, times(1)).login(any(UserLoginRequest.class));
	}

	/**
	 * 유요한 Barer 헤더로 로그아웃을 시도하여 200OK 응답을 확인하고
	 * AuthService#logout(String)이 호출되는지 검증합니다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("성공: 유효한 Bearer 토큰으로 로그아웃 요청 시 200 OK")
	void logout_success() throws Exception {
		// given
		final String bearerToken = "Bearer " + VALID_ACCESS_TOKEN;
		doNothing().when(authService).logout(eq(VALID_ACCESS_TOKEN));

		// when & then
		mockMvc.perform(
				post(LOGOUT_URL)
					.header("Authorization", bearerToken)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("로그아웃이 성공적으로 처리되었습니다."))
			.andDo(print());

		verify(authService, times(1)).logout(eq(VALID_ACCESS_TOKEN));
	}

	/**
	 * Authorization  누락시 Controller 에서 예외를 던지고 401 응답을확인하빈다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("실패: Authorization 헤더 누락 시 401 Unauthorized")
	void logout_fail_no_header() throws Exception {
		// given
		ErrorCode errorCode = ErrorCode.AUTH_INVALID_TOKEN_FORMAT;

		// when & then
		mockMvc.perform(
				post(LOGOUT_URL)
			)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(errorCode.getCode()))
			.andDo(print());

		verify(authService, never()).logout(any());
	}

	/**
	 * Authorization 헤더에 Barer 누락된경우 Controller에서 예외를 던지고 401응답을 확인합니다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("실패: Bearer 접두사 누락 시 401 Unauthorized")
	void logout_fail_missing_bearer() throws Exception {
		// given
		final String invalidToken = VALID_ACCESS_TOKEN;
		ErrorCode errorCode = ErrorCode.AUTH_INVALID_TOKEN_FORMAT;

		// when & then
		mockMvc.perform(
				post(LOGOUT_URL)
					.header("Authorization", invalidToken)
			)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(errorCode.getCode()))
			.andDo(print());
		verify(authService, never()).logout(any());
	}
}
