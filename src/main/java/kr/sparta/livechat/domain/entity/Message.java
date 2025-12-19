package kr.sparta.livechat.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.sparta.livechat.domain.role.MessageType;
import kr.sparta.livechat.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방에서 전송되는 메시지를 나타내는 엔티티입니다.
 * <p>
 * 메시지는 특정 채팅방에 종속되며, 작성자와 전송시각을 가집니다.
 * 채팅방 생성 시 첫 문의 메시지가 함께 생성될 수 있습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "messages",
	indexes = {
		@Index(name = "idx_messages_room_sent_at", columnList = "room_id, sent_at")
	}
)
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Lob
	@Column(nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MessageType type;

	@Column(name = "sent_at", nullable = false)
	private LocalDateTime sentAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "writer_id", nullable = false)
	private User writer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "room_id", nullable = false)
	private ChatRoom room;

	/**
	 * 메시지를 생성합니다.
	 *
	 * @param room    메시지가 속한 채팅방
	 * @param writer  메시지 작성자
	 * @param content 메시지 내용
	 * @param type    메시지 유형
	 * @return 생성된 메시지
	 */
	public static Message of(ChatRoom room, User writer, String content, MessageType type) {
		Message message = new Message();
		message.room = room;
		message.writer = writer;
		message.content = content;
		message.type = type;
		message.sentAt = LocalDateTime.now();
		return message;
	}
}
