package kr.sparta.livechat.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 성공후 서버가 클라이언트에게 전달되는 응답 DTO 클래스 입니다.
 * AccessToken 과 Refresh Token을 포합합니다.
 * UserLoginResponse.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@Getter
@Builder
public class UserLoginResponse {
	private String accessToken;
	private String refreshToken;
}
