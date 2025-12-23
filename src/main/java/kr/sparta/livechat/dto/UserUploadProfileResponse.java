package kr.sparta.livechat.dto;

import lombok.Getter;

/**
 * UserUploadProfileResponse 클래스입니다.
 * <p>
 * 프로필 이미지 업로드 이후 수정 성공 시 전달되는 DTO 클래스입니다.
 * </p>
 * @author 변채주
 * @version 1.0
 * @since 2025. 12. 23.
 */
@Getter
public class UserUploadProfileResponse {
	private final Long userId;
	private final String name;
	private final String profileImageUrl;

	public UserUploadProfileResponse(Long userId, String name, String profileImageUrl) {
		this.userId = userId;
		this.name = name;
		this.profileImageUrl = profileImageUrl;
	}
}
