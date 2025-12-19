package kr.sparta.livechat.domain.role;

import kr.sparta.livechat.entity.Role;

/**
 * 채팅방 내에서의 사용자 역할을 나타내는 enum입니다.
 * <p>
 * 채팅방 참여자는 구매자(BUYER) 또는 판매자(SELLER)만 될 수 있습니다.
 * 관리자는 채팅방에 참여하지 않으며, 조회/상태 변경과 같은 권한은 시스템 권한({@link Role}) 기반으로 별도 인가 로직에서 처리합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
public enum RoleInRoom {
	BUYER,
	SELLER
}
