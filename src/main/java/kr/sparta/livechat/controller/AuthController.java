package kr.sparta.livechat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.sparta.livechat.dto.UserRegisterRequest;
import kr.sparta.livechat.dto.UserRegisterResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.service.AuthService;
import lombok.RequiredArgsConstructor;

/**
 * 인증 및 회원가입 관련 API 요청을 처리하는 Controller 클래스입니다.
 * 회원가입 요청을 받아 Service 계층으로 위임하고,
 *  요청/응답에 대한 Validation 및 HTTP 상태 코드를 관리합니다.
 * AuthController.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 */
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
}
