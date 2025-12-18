package kr.sparta.livechat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청정보를 담는 DTO클래스입니다.
 * 클라이언트로부터 이메일과 비밀번호 전달 받으며.
 * jakarta.validation.Valid 를 통해
 * 서버에서 유효성 검사를 합니다.
 * UserLoginRequest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {

	@NotBlank(message = "이메일은 필수 입력 항목입니다.")
	@Email(message = "유효한 이메일 형식이 아닙니다.")
	private String email;

	@NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
	private String password;
}
