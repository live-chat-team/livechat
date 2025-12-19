package kr.sparta.livechat.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
import lombok.Getter;

/**
 * 상품 문의를 위한 1:1 상담 채팅방을 나타내는 엔티티입니다.
 * <p>
 * 채팅방은 특정 상품에 대한 채팅방이며, 방의 상태와 생성/종료 시각을 관리합니다.
 * 채팅방에는 구매자/판매자가 참여자로 등록되며, 채팅방 생성 시 [채팅방 생성 + 참여자 등록 + 첫 메시지 전송] 이 하나의 트랜잭션으로 보장됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
@Entity
@Getter
@Table(name = "chat_rooms")
public class ChatRoom extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ChatRoomStatus status;

	@Column(name = "opened_at", nullable = false)
	private LocalDateTime openedAt;

	@Column(name = "closed_at", nullable = false)
	private LocalDateTime closedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ChatRoomParticipant> participants = new ArrayList<>();

	/**
	 * 특정 상품에 대한 상담을 위해 OPEN 상태의 채팅방을 생성합니다.
	 *
	 * @param product 상담 대상 상품
	 * @return OPEN 상태로 개설된 채팅방
	 */
	public static ChatRoom open(Product product) {
		ChatRoom room = new ChatRoom();
		room.product = product;
		room.status = ChatRoomStatus.OPEN;
		room.openedAt = LocalDateTime.now();
		return room;
	}

	/**
	 * 채팅방을 종료 처리합니다.
	 * 종료 일시는 종료 호출이 된 시간을 기준으로 설정합니다.
	 */
	public void close() {
		this.status = ChatRoomStatus.CLOSED;
		this.closedAt = LocalDateTime.now();
	}
}
