package kr.sparta.livechat.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.sparta.livechat.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지의 읽음 처리를 나타내는 엔티티입니다.
 * <p>
 * 특정 사용자가 특정 메시지를 언제 읽었는지 기록하빈다.
 * 동일한 메시지에 대해 동일 사용자의 중복 기록을 방지하기 위해 유니크 제약을 설정합니다.
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
	name = "message_read",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_message_user", columnNames = {"message_id", "user_id"})
	},
	indexes = {
		@Index(name = "idx_message_read_user", columnList = "user_id"),
		@Index(name = "idx_message_read_message", columnList = "message_id")
	}
)
public class MessageRead {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "read_at", nullable = false)
	private LocalDateTime readAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "message_id", nullable = false)
	private Message message;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/**
	 * 메시지 읽음 여부를 기록합니다.
	 *
	 * @param message 읽음 대상 메시지
	 * @param user    메시지를 읽은 사용자
	 * @return 생성된 메시지 읽음 기록
	 */
	public static MessageRead of(Message message, User user) {
		MessageRead read = new MessageRead();
		read.message = message;
		read.user = user;
		read.readAt = LocalDateTime.now();
		return read;
	}
}
