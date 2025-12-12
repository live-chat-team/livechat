package kr.sparta.livechat.dto;

import java.time.LocalDateTime;

import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 완료 후 반환되는 사용자 정보를 담는 DTO입니다.
 * 회원가입이 성공적으로 처리된 후 사용자 ID, 이메일, 이름, 역할, 생성 시각을
 * 클라이언트에 전달하는 데 사용됩니다. 엔티티 user 객체를 기반으로 생성됩니다.
 * UserRegisterResponse
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 *
 */
@Getter
@Builder
@AllArgsConstructor
public class UserRegisterResponse {

	/**
	 * 사용자 고유 ID
	 */
	private final Long id;

	/**
	 * 사용자 이메일
	 */
	private final String email;

	/**
	 * 사용자 이름
	 */
	private final String name;

	/**
	 * 사용자 역할
	 */
	private final Role role;

	/**
	 * 사용자 생성 시각
	 */
	private final LocalDateTime createdAt;

	/**
	 * user 엔티티를 기반으로 응답 DTO를 생성합니다.
	 *
	 * @param user 회원가입 완료된 사용자 엔티티
	 * @return UserRegisterResponse DTO
	 */
	public static UserRegisterResponse from(User user) {
		return UserRegisterResponse.builder()
			.id(user.getId())
			.email(user.getEmail())
			.name(user.getName())
			.role(user.getRole())
			.createdAt(user.getCreatedAt())
			.build();
	}
}
