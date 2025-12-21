package kr.sparta.livechat.dto.user;

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
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {
	private String email;
	private String password;
	private String name;
	private Role role;
}
