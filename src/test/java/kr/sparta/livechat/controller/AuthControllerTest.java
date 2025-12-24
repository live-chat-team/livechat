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

import kr.sparta.livechat.dto.user.UserLoginRequest;
import kr.sparta.livechat.dto.user.UserLoginResponse;
import kr.sparta.livechat.dto.user.UserRegisterRequest;
import kr.sparta.livechat.dto.user.UserRegisterResponse;
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

	private UserRegisterResponse mockRegisterResponse() {
		return UserRegisterResponse.builder()
			.id(1L)
			.email(VALID_EMAIL)
			.name(VALID_NAME)
			.role(Role.BUYER)
			.createdAt(LocalDateTime.now())
			.build();
	}

	private UserLoginResponse mockLoginResponse() {
		return UserLoginResponse.builder()
			.accessToken(VALID_ACCESS_TOKEN)
			.refreshToken("refresh.jwt.token")
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
		UserRegisterResponse response = mockRegisterResponse();

		when(authService.registerUser(any(UserRegisterRequest.class)))
			.thenReturn(response);

		// when
		mockMvc.perform(
				post(REGISTER_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			//then
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
	@DisplayName("회원가입 실패 - 이메일 중복")
	void register_fail_duplicate_email() throws Exception {
		//given
		UserRegisterRequest request = validRequest();
		ErrorCode errorCode = ErrorCode.AUTH_DUPLICATE_EMAIL;

		doThrow(new CustomException(errorCode))
			.when(authService).registerUser(any(UserRegisterRequest.class));

		//whtn
		mockMvc.perform(
				post(REGISTER_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			//then
			.andExpect(status().is(errorCode.getStatus().value()))
			.andExpect(jsonPath("$.code").value(errorCode.getCode()))
			.andExpect(jsonPath("$.message").value(errorCode.getMessage()))
			.andDo(print());

		verify(authService, times(1)).registerUser(any(UserRegisterRequest.class));
	}

	/**
	 * 회원가입 실패테스트-ADMIN 역할요청
	 * 권환 제한으로 예외 발생
	 * @throws Exception 예외
	 */
	@Test
	@DisplayName("회원가입 실패 - ADMIN 역할 시도")
	void register_fail_forbidden_admin_role() throws Exception {
		//given
		UserRegisterRequest request = new UserRegisterRequest(
			VALID_EMAIL,
			VALID_PASSWORD,
			VALID_NAME,
			Role.ADMIN
		);
		ErrorCode errorCode = ErrorCode.AUTH_FORBIDDEN_ROLE;

		doThrow(new CustomException(errorCode))
			.when(authService).registerUser(any(UserRegisterRequest.class));

		//when
		mockMvc.perform(
				post(REGISTER_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)

			//then
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
	@DisplayName("로그인 성공 200 OK")
	void login_success() throws Exception {
		// given
		UserLoginRequest request = new UserLoginRequest(VALID_EMAIL, VALID_PASSWORD);
		UserLoginResponse response = mockLoginResponse();

		when(authService.login(any(UserLoginRequest.class))).thenReturn(response);

		// when
		mockMvc.perform(
				post(LOGIN_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)

			//then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value(VALID_ACCESS_TOKEN))
			.andExpect(jsonPath("$.refreshToken").value("refresh.jwt.token"))
			.andDo(print());

		verify(authService, times(1)).login(any(UserLoginRequest.class));
	}

	/**
	 * Service 에서 인증 정보 불일치시 CustomException 던질경우
	 * GlobalExceptionHandler를 통해 401 응답이 오는지 확인합니다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("로그인 실패 - 인증 정보 불일치")
	void login_fail_invalid_credentials() throws Exception {
		// given
		UserLoginRequest request = new UserLoginRequest(VALID_EMAIL, "wrongpassword");
		ErrorCode errorCode = ErrorCode.AUTH_INVALID_CREDENTIALS;

		doThrow(new CustomException(errorCode))
			.when(authService).login(any(UserLoginRequest.class));

		// when
		mockMvc.perform(
				post(LOGIN_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)

			//then
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(errorCode.getCode()))
			.andExpect(jsonPath("$.message").value(errorCode.getMessage()))
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

		// when
		mockMvc.perform(
				post(LOGOUT_URL)
					.header("Authorization", bearerToken)
			)

			//then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message")
				.value("로그아웃 되었습니다.쿠키가 삭제되었습니다."))
			.andDo(print());

		verify(authService, times(1)).logout(eq(VALID_ACCESS_TOKEN));
	}

	/**
	 * Authorization  누락시 Controller 에서 예외를 던지고 401 응답을확인하빈다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("로그아웃 성공 - 헤더 누락")
	void logout_no_header_success() throws Exception {
		//given

		//when
		mockMvc.perform(post(LOGOUT_URL))
			//then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message")
				.value("로그아웃 되었습니다.쿠키가 삭제되었습니다."))
			.andDo(print());

		verify(authService, never()).logout(any());
	}

	/**
	 * Authorization 헤더에 Barer 누락된경우 Controller에서 예외를 던지고 401응답을 확인합니다.
	 * @throws Exception Exception MockMvc 수행 중 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("로그아웃 성공 - Bearer 누락")
	void logout_missing_bearer_success() throws Exception {
		//given

		//when
		mockMvc.perform(
				post(LOGOUT_URL)
					.header("Authorization", VALID_ACCESS_TOKEN)
			)
			//then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message")
				.value("로그아웃 되었습니다.쿠키가 삭제되었습니다."))
			.andDo(print());

		verify(authService, never()).logout(any());
	}
}
