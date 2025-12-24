package kr.sparta.livechat.dto.chatroom;

import kr.sparta.livechat.domain.role.RoleInRoom;
import lombok.Getter;

/**
 * 채팅방 참여자 목록의 항목 DTO입니다.
 * <p>
 * 채팅방 상세 조회 응답에서 참여자 1명의 기본 정보를 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 21.
 */
@Getter
public class ParticipantsListItem {
	private final Long userId;
	private final String userName;
	private final RoleInRoom roleInRoom;

	/**
	 * 채팅방에 참여한 참여자의 기본 정보를 담습니다.
	 *
	 * @param userId     참여자 고유 식별자
	 * @param userName   참여자 이름
	 * @param roleInRoom 채팅방 내 참여자의 역할 (BUYER / SELLER)
	 */
	public ParticipantsListItem(Long userId, String userName, RoleInRoom roleInRoom) {
		this.userId = userId;
		this.userName = userName;
		this.roleInRoom = roleInRoom;
	}
}
