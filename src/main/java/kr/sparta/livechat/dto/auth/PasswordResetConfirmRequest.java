package kr.sparta.livechat.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 인증 코드 검증 및 비밀번호 변경 요청 DTO입니다.
 * <p>
 * 이메일, 인증 코드, 새 비밀번호를 전달받아
 * 비밀번호 재설정 확정 단계에서 사용됩니다.
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 24.
 */
@Getter
@NoArgsConstructor
public class PasswordResetConfirmRequest {

	/**
	 * 비밀번호 재설정 대상 사용자 이메일
	 */
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;

	/**
	 * 이메일로 전송된 6자리 인증 코드
	 */
	@NotBlank(message = "인증 코드는 필수 입력값입니다.")
	@Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
	private String code;

	/**
	 * 새 비밀번호
	 * <p>
	 * (기존 회원가입/로그인 비밀번호 정책과 동일하게 유지)
	 * </p>
	 */
	@NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
		message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
	)
	private String newPassword;
}
