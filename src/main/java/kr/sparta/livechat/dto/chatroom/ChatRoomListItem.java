package kr.sparta.livechat.dto.chatroom;

import java.time.LocalDateTime;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
import lombok.Getter;

/**
 * 채팅방 목록 조회 시, 목록 내 단일 채팅방 정보를 담는 DTO입니다.
 * <p>
 * 채팅방 목록 관련 필요한 필드만 포함하며, {@link ChatRoom} 엔티티를 DTO로 변환하여 사용합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 20.
 */
@Getter
public class ChatRoomListItem {

	private final Long chatRoomId;
	private final ChatRoomStatus status;
	private final String productName;
	private final String opponentName;
	private final LocalDateTime lastMessageSentAt;

	public ChatRoomListItem(ChatRoom chatRoom, Long currentUserId) {
		this.chatRoomId = chatRoom.getId();
		this.status = chatRoom.getStatus();
		this.productName = chatRoom.getProduct().getName();
		this.opponentName = chatRoom.getParticipants().stream()
			.filter(p -> !p.getUser().getId().equals(currentUserId))
			.findFirst()
			.map(p -> p.getUser().getName())
			.orElse(null);
		this.lastMessageSentAt = chatRoom.getLastMessageSentAt();
	}
}
