package kr.sparta.livechat.dto.message;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 채팅방 메시지 목록 조회 API 응답 DTO 클래스입니다.
 * <p>
 * 채팅방에서 주고받은 메시지 목록을 조회할 때 사용되며,
 * 스크롤 기반 조회를 지원하여 최신 메시지부터 과거 메시지까지 단계적으로 조회할 수 있습니다.
 * 서비스 계층에서 호출 시에는 채팅방 화면에 응답하는 방식과 유사하게 전송 시각 기준 오름차순으로 정렬된 상태로 반환됩니다.
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
	private final int page;
	private final int size;
	private final boolean hasNext;
	private final List<ChatMessageListItem> messagesList;

	/**
	 * 메시지 목록 조회 응답 DTO를 생성합니다.
	 *
	 * @param chatRoomId  메시지를 조회한 채팅방 식별자
	 * @param page        조회에 사용된 페이지 번호
	 * @param size        조회된 메시지 개수
	 * @param hasNext     추가 조회할 메시지의 존재 여부
	 * @param messageList 조회된 메시지 목록
	 */
	public GetChatMessageListResponse(Long chatRoomId, int page, int size, boolean hasNext,
		List<ChatMessageListItem> messageList) {
		this.chatRoomId = chatRoomId;
		this.page = page;
		this.size = size;
		this.hasNext = hasNext;
		this.messagesList = messageList;
	}
}
