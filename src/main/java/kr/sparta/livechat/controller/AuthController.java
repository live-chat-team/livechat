package kr.sparta.livechat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.sparta.livechat.dto.UserLoginRequest;
import kr.sparta.livechat.dto.UserLoginResponse;
import kr.sparta.livechat.dto.UserLogoutResponse;
import kr.sparta.livechat.dto.UserRegisterRequest;
import kr.sparta.livechat.dto.UserRegisterResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증 및 회원가입 관련 API 요청을 처리하는 Controller 클래스입니다.
 * 회원가입 , 로그인, 로그아웃 요청을 받아 Service 계층으로 위임하고,
 *  요청/응답에 대한 Validation 및 HTTP 상태 코드를 관리합니다.
 * AuthController.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * 회원가입 요청을 처리합니다.
	 * 클라이언트로부터 전달받은 회원가입 정보를 기반으로
	 * 새로운 사용자를 생성하고 저장합니다.
	 *
	 * @param request 회원가입 요청 데이터
	 * @return 생성된 사용자 정보와 함께 201(Created) 응답
	 */
	@PostMapping("/register")
	public ResponseEntity<UserRegisterResponse> register(
		@Valid @RequestBody UserRegisterRequest request) {

		if (request.getRole() == Role.ADMIN) {
			throw new CustomException(ErrorCode.AUTH_FORBIDDEN_ROLE);
		}
		UserRegisterResponse response = authService.registerUser(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 로그인 요청을 처리합니다.
	 * 인증 성공시 Access Token 과 Refresh Token을 포함하여 응답합니다.
	 *
	 * @param request 로그인 요청 데이터(이메일, 비밀번호)
	 * @return JWT 토큰 정보를 포함하여 응답200(OK) 응답
	 */

	@PostMapping("/login")
	public ResponseEntity<UserLoginResponse> login(
		@Valid @RequestBody UserLoginRequest request) {
		UserLoginResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 로그아웃 요청을 처리합니다.
	 * Access Token을 추출하여 서버 측 블랙리스트에 등록합니다.
	 *
	 * @param authorizationHeader 요청 헤더의 Authorization 값
	 * @return 로그아웃 성공 메세지를 포함하여 응답과 함께 200(ok) d응답
	 * @throws CustomException Access Token 이 누락 되거나 Bearer 형식이 아닐경우 예외 발생
	 */
	@PostMapping("/logout")
	public ResponseEntity<UserLogoutResponse> logout(
		@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
		String accessToken = null;

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			accessToken = authorizationHeader.substring(7);
		}
		if (accessToken == null) {
			log.warn("로그아웃 요청에 Access Token이 누락되었습니다.");
			throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN_FORMAT);
		}
		authService.logout(accessToken);
		UserLogoutResponse response = UserLogoutResponse.builder()
			.message("로그아웃이 성공적으로 처리되었습니다.")
			.build();

		return ResponseEntity.ok(response);
	}
}
