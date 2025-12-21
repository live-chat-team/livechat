package kr.sparta.livechat.dto.chatroom;

import java.time.LocalDateTime;

import kr.sparta.livechat.domain.role.ChatRoomStatus;
import lombok.Getter;

/**
 * 채팅방 상태 변경 요청에 따른 응답을 전달하기 위한 DTO 클래스입니다.
 * <p>
 * 응답에서는 채팅방의 기본 정보와 요청값에 따른 채팅방의 상태, 그리고 변경 사유, 종료일시가 갱신됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 21.
 */
@Getter
public class PatchChatRoomResponse {

	private final Long chatRoomId;
	private final ChatRoomStatus status;
	private final String reason;
	private final LocalDateTime closedAt;

	/**
	 * 채팅방 상태 변경 요청에 따른 응답 생성자입니다.
	 *
	 * @param chatRoomId 채팅방 고유 식별자
	 * @param status     채팅방 상태
	 * @param reason     채팅방 상태 변경 사유(선택)
	 * @param closedAt   채팅방 상태 변경 시 종료일시
	 */
	public PatchChatRoomResponse(Long chatRoomId, ChatRoomStatus status, String reason, LocalDateTime closedAt) {
		this.chatRoomId = chatRoomId;
		this.status = status;
		this.reason = reason;
		this.closedAt = closedAt;
	}
}
