package kr.sparta.livechat.dto.admin;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자의 채팅방 상태 변경 결과 응답 DTO
 * 변경된 채팅방의 상태 정보와 시작, 종료 시각
 * 해당 상품 정보와 상품 이름을 포함
 * AdminChatStatusResponse.java
 *
 * @author kimsehyun
 * @since 2025. 12. 23.
 */
@Getter
@Builder
@AllArgsConstructor
public class AdminChatStatusResponse {
	private Long chatRoomId;
	private String status;
	private LocalDateTime openedAt;
	private LocalDateTime closedAt;
	private Long productId;
	private String productName;
}
