package kr.sparta.livechat.dto.chatroom;

import java.util.List;

import lombok.Getter;

/**
 * 채팅방 목록 조회 API의 응답 DTO입니다.
 * <p>
 * 페이지네이션 정보(page/size/totalElements/totalPages/hasNext)와
 * 목록 항목({@link ChatRoomListItem}) 리스트를 함께 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 20.
 */
@Getter
public class GetChatRoomListResponse {
	private final int page;
	private final int size;
	private final Long totalElements;
	private final int totalPages;
	private final boolean hasNext;
	private final List<ChatRoomListItem> chatRoomList;

	/**
	 * 채팅방 목록 조회 응답 DTO를 생성합니다.
	 *
	 * @param page          현재 페이지 (0부터 시작)
	 * @param size          페이지 크기
	 * @param totalElements 전체 채팅방 수
	 * @param totalPages    전체 페이지 수
	 * @param hasNext       다음 페이지 존재 여부
	 * @param chatRoomList  채팅방 목록 리스트
	 */
	public GetChatRoomListResponse(int page, int size, Long totalElements, int totalPages, boolean hasNext,
		List<ChatRoomListItem> chatRoomList) {
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.hasNext = hasNext;
		this.chatRoomList = chatRoomList;
	}
}
