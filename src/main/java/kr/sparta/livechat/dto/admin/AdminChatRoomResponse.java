package kr.sparta.livechat.dto.admin;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자용 채팅방 개별 정보 응답 DTO
 * 채팅방 목록에 표시될 정보
 * 상풍 정보, 참여자(판매자,구매자) 정보
 * AdminChatRoomResponse.java
 *
 * @author kimsehyun
 * @since 2025. 12. 18.
 */
@Getter
@Builder
@AllArgsConstructor
public class AdminChatRoomResponse {
	private Long chatRoomId;
	private String status;
	private LocalDateTime openedAt;
	private LocalDateTime closedAt;
	private String productName;
	private String sellerName;
	private String buyerName;
	private LocalDateTime lastMessageSentAt;
}
