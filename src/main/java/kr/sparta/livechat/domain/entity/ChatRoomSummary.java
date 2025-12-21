package kr.sparta.livechat.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ChatRoomSummary 클래스입니다.
 * <p>
 * 상담 채팅방 종료 시 상담 요약 정보를 저장하기 위한 엔티티입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 21.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_rooms_summary")
public class ChatRoomSummary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_id", nullable = false, unique = true)
	private Long roomId;

	private long totalMessageCount;
	private long durationSeconds;
	private LocalDateTime closedAt;

	/**
	 * 상담 종료 시점의 요약 정보를 담는 {@link ChatRoomSummary} 엔티티를 생성합니다.
	 *
	 * @param roomId            상담 채팅방 식별자
	 * @param totalMessageCount 총 메시지 수
	 * @param durationSeconds   상담 지속 시간(초)
	 * @param closedAt          상담 종료 시각
	 * @return 생성된 {@link ChatRoomSummary} 엔티티
	 */
	public static ChatRoomSummary of(Long roomId, long totalMessageCount, long durationSeconds,
		LocalDateTime closedAt) {
		ChatRoomSummary summary = new ChatRoomSummary();
		summary.roomId = roomId;
		summary.totalMessageCount = totalMessageCount;
		summary.durationSeconds = durationSeconds;
		summary.closedAt = closedAt;
		return summary;
	}
}
