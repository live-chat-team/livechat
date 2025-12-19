package kr.sparta.livechat.domain.entity;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.sparta.livechat.domain.role.RoleInRoom;
import kr.sparta.livechat.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방에 참여하는 사용자를 나타내는 엔티티입니다.
 * <p>
 * 채팅방 참여자는 구매자(BUYER) 또는 판매자(SELLER)로 구분되며, 하나의 채팅방에 동일 사용자가 중복 참여하는 것을 방지합니다.
 * 채팅방 참여자는 채팅방({@link ChatRoom})에 종속되며, 채팅방의 생명주기에 따라 함께 관리됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
@Entity
@Getter
@Table(
	name = "chat_room_participants",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_room_user",
			columnNames = {"room_id", "user_id"}
		)
	})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomParticipant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_in_room", nullable = false)
	private RoleInRoom roleInRoom;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "room_id", nullable = false)
	private ChatRoom room;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/**
	 * 채팅방 참여자를 생성합니다.
	 *
	 * @param room       참여할 채팅방
	 * @param user       참여 사용자
	 * @param roleInRoom 채팅방 내 역할 (BUYER / SELLER)
	 * @return 생성된 채팅방 참여자
	 */
	public static ChatRoomParticipant of(ChatRoom room, User user, RoleInRoom roleInRoom) {
		ChatRoomParticipant participant = new ChatRoomParticipant();
		participant.room = room;
		participant.user = user;
		participant.roleInRoom = roleInRoom;
		return participant;
	}
}
