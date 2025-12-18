package kr.sparta.livechat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 로그아웃 성공시 서버가 클래스에 전달하는 응답DTO 클래스입니다,
 * 로그아웃 성공 여부를 클라이언트에게 알리는 메세지가 포함되어있습니다.
 * UserLogoutResponse.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@Builder
@Getter
@RequiredArgsConstructor
public class UserLogoutResponse {
	private final String message;
}

