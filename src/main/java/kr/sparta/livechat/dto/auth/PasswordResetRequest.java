package kr.sparta.livechat.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 인증 코드 발송 요청 DTO입니다.
 * <p>
 * 사용자가 비밀번호 재설정을 요청할 때 이메일을 전달합니다.
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 24.
 */
@Getter
@NoArgsConstructor
public class PasswordResetRequest {

	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;
}
