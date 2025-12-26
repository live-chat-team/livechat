package kr.sparta.livechat.dto.message;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 채팅방 메시지 목록 조회 API 응답 DTO 클래스입니다.
 * <p>
 * 채팅방에서 주고받은 메시지 목록을 조회할 때 사용되며,
 * 스크롤 기반 조회를 지원하기 위해 커서(cursor) 기반 페이지네이션을 사용합니다.
 * 최신 메시지부터 과거 메시지까지 단계적으로 조회할 수 있습니다.
 * 서비스 계층에서는 화면 렌더링을 고려하여 전송 시각 기준 오름차순으로 정렬된 상태로 반환됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 22.
 */
@Getter
@Builder
public class GetChatMessageListResponse {
	private final Long chatRoomId;
	private final int size;
	private final boolean hasNext;
	private final Long nextCursor;
	private final List<ChatMessageListItem> messageList;

	/**
	 * 메시지 목록 조회 응답 DTO를 생성합니다.
	 *
	 * @param chatRoomId  메시지를 조회한 채팅방 식별자
	 * @param size        조회에 사용된 페이지 크기
	 * @param hasNext     추가 조회할 메시지의 존재 여부
	 * @param nextCursor  다음 조회에 사용할 커서(가장 오래된 메시지 ID). 추가 조회가 없으면 null
	 * @param messageList 조회된 메시지 목록
	 */
	public GetChatMessageListResponse(
		Long chatRoomId,
		int size,
		boolean hasNext,
		Long nextCursor,
		List<ChatMessageListItem> messageList
	) {
		this.chatRoomId = chatRoomId;
		this.size = size;
		this.hasNext = hasNext;
		this.nextCursor = nextCursor;
		this.messageList = messageList;
	}
}
