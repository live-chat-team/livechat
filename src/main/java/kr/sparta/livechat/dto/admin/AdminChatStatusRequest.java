package kr.sparta.livechat.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자의 채팅방 상태 변경 요청을 처리하는 DTO
 * 특정 채팅방을 종료 할때 사용
 * AdminChatStatusRequest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 23.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminChatStatusRequest {
	private String status;
}
