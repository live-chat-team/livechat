package kr.sparta.livechat.dto.chatroom;

import java.time.LocalDateTime;
import java.util.List;

import kr.sparta.livechat.domain.role.ChatRoomStatus;
import lombok.Getter;

/**
 * 채팅방 상세 조회 결과를 반환하는 응답 DTO 클래스입니다.
 * <p>
 * 채팅방 상태/개설·종료 시각, 상품 기본 정보, 참여자 목록을 함께 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 21.
 */
@Getter
public class GetChatRoomDetailResponse {
	private final Long chatRoomId;
	private final ChatRoomStatus status;
	private final LocalDateTime openedAt;
	private final LocalDateTime closedAt;
	private final ProductInfo productInfo;
	private final List<ParticipantsListItem> participantsListItems;

	/**
	 * 채팅방 상세세 조회 응답 DTO를 생성합니다.
	 *
	 * @param chatRoomId       채팅방 고유 식별자
	 * @param status           채팅방 상태 (OPEN / CLOSED)
	 * @param openedAt         채팅방 개설일시
	 * @param closedAt         채팅방 종료일시
	 * @param productInfo      상담 채팅방 생성에 사용된 상품 기본 정보
	 * @param participantsList 채팅방 참여자 목록
	 */
	public GetChatRoomDetailResponse(Long chatRoomId, ChatRoomStatus status, LocalDateTime openedAt,
		LocalDateTime closedAt, ProductInfo productInfo, List<ParticipantsListItem> participantsList) {
		this.chatRoomId = chatRoomId;
		this.status = status;
		this.openedAt = openedAt;
		this.closedAt = closedAt;
		this.productInfo = productInfo;
		this.participantsListItems = participantsList;
	}
}
