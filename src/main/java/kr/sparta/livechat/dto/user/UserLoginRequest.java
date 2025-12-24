package kr.sparta.livechat.dto.user;

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

	private String email;
	private String password;
}
