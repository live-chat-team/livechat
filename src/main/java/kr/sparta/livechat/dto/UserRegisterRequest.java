package kr.sparta.livechat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import kr.sparta.livechat.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 정보를 담는 DTO 클래스입니다.
 * 이 클래스는 회원가입 시 클라이언트로부터 전달되는 이메일, 비밀번호,
 * 이름, 역할 정보를 포함하며, Jakarta Validation을 통해 입력값 유효성을 검사합니다.
 * UserRegisterRequest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 *
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;

	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
		message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
	)
	private String password;

	@NotBlank(message = "이름은 필수 입력값입니다.")
	private String name;

	@NotNull(message = "역할(Role)은 필수 입력값입니다.")
	private Role role;
}
