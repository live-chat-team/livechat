package kr.sparta.livechat.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자용 채팅방 상세 조회 응답 DTO 클래스
 * 채팅방의 기본 정보, 메시지 목록을 데이터와 함께 제공합니다.
 * AdminChatDetailResponse.java
 *
 * @author kimsehyun
 * @since 2025. 12. 22.
 */

@Getter
@Builder
@AllArgsConstructor
public class AdminChatDetailResponse {
	private Long chatRoomId;
	private String chatRoomStatus;

	private int page;
	private int size;
	private boolean hasNext;

	private List<AdminChatMessageResponse> messagesList;
}
