package kr.sparta.livechat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.sparta.livechat.dto.auth.PasswordResetConfirmRequest;
import kr.sparta.livechat.dto.auth.PasswordResetRequest;
import kr.sparta.livechat.service.PasswordResetService;
import lombok.RequiredArgsConstructor;

/**
 * 비밀번호 재설정 API 컨트롤러입니다.
 * <p>
 * 로그인 없이 이메일 인증 코드를 발급받고,
 * 인증 코드 검증 후 비밀번호를 변경합니다.
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 24.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

	private final PasswordResetService passwordResetService;

	/**
	 * 비밀번호 재설정 인증 코드 발송 요청
	 *
	 * @param request 이메일 정보
	 * @return 200 OK
	 */
	@PostMapping("/request")
	public ResponseEntity<Void> requestReset(@Valid @RequestBody PasswordResetRequest request) {
		passwordResetService.requestReset(request.getEmail());
		return ResponseEntity.ok().build();
	}

	/**
	 * 인증 코드 검증 및 비밀번호 변경 확정
	 *
	 * @param request 이메일, 인증 코드, 새 비밀번호
	 * @return 200 OK
	 */
	@PostMapping("/confirm")
	public ResponseEntity<Void> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
		passwordResetService.confirmReset(
			request.getEmail(),
			request.getCode(),
			request.getNewPassword()
		);
		return ResponseEntity.ok().build();
	}
}
