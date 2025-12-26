package kr.sparta.livechat.dto.message;

import java.time.LocalDateTime;

import kr.sparta.livechat.domain.entity.Message;
import kr.sparta.livechat.domain.role.MessageType;
import lombok.Getter;

/**
 * 메시지 목록 조회 간 단일 메시지 정보를 담는 DTO 클래스입니다.
 * <p>
 * 메시지에 필요한 최소 정보를 담습니다.
 * DB에 저장된 가장 최신 메시지를 기준으로 조회할 페이지의 크기만큼 추출한 후
 * 실제 채팅방의 대화 내용과 유사한 형태로 전송 시각 기준 오름차순으로 정렬합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 22.
 */
@Getter
public class ChatMessageListItem {

	private final Long messageId;
	private final Long writerId;
	private final String content;
	private final MessageType messageType;
	private final LocalDateTime sentAt;

	private ChatMessageListItem(Message message) {
		this.messageId = message.getId();
		this.writerId = message.getWriter().getId();
		this.content = message.getContent();
		this.messageType = message.getType();
		this.sentAt = message.getSentAt();
	}

	/**
	 * 엔티티를 메시지 목록 조회용 DTO로 변환합니다.
	 *
	 * @param message 변환할 메시지 엔티티
	 * @return 메시지 목록 조회에서 사용할 단일 메시지 정보
	 */
	public static ChatMessageListItem from(Message message) {
		return new ChatMessageListItem(message);
	}
}
